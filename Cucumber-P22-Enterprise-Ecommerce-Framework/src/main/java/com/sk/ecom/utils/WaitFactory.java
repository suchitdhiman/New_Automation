package com.sk.ecom.utils;

import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.driver.DriverManager;
import com.sk.ecom.enums.WaitStrategy;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * All synchronisation in the framework funnels through here.
 *
 * <p>No {@code Thread.sleep} exists anywhere in this project. Interactions
 * declare <em>why</em> they are waiting via {@link WaitStrategy}, and this class
 * turns that intent into an {@link ExpectedCondition}. That is what makes the
 * suite stable enough to run in parallel on a loaded CI agent.
 */
public final class WaitFactory {

	private WaitFactory() {
		throw new IllegalStateException("Utility class");
	}

	private static WebDriver driver() {
		return DriverManager.getDriver();
	}

	public static WebDriverWait wait() {
		return new WebDriverWait(driver(), FrameworkConstants.explicitWait(), FrameworkConstants.pollingInterval());
	}

	public static WebDriverWait wait(Duration timeout) {
		return new WebDriverWait(driver(), timeout, FrameworkConstants.pollingInterval());
	}

	/**
	 * @return the element once the strategy is satisfied, or {@code null} for
	 *         {@link WaitStrategy#INVISIBLE} where there is nothing to return.
	 */
	public static WebElement waitFor(By locator, WaitStrategy strategy) {
		return switch (strategy) {
			case CLICKABLE -> wait().until(ExpectedConditions.elementToBeClickable(locator));
			case VISIBLE -> wait().until(ExpectedConditions.visibilityOfElementLocated(locator));
			case PRESENCE -> wait().until(ExpectedConditions.presenceOfElementLocated(locator));
			case ALL_VISIBLE -> wait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator)).get(0);
			case INVISIBLE -> {
				wait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
				yield null;
			}
			case NONE -> driver().findElement(locator);
		};
	}

	public static List<WebElement> waitForAll(By locator) {
		return wait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
	}

	/* ------------------------------------------------------------------ */
	/* Fluent / custom conditions                                          */
	/* ------------------------------------------------------------------ */

	/**
	 * Fluent wait that ignores the three exceptions a re-rendering SPA throws
	 * while it settles. Use for anything that is not a plain "element appears".
	 */
	public static <T> T fluentlyUntil(Function<WebDriver, T> condition, Duration timeout, String description) {
		return new FluentWait<>(driver())
				.withTimeout(timeout)
				.pollingEvery(FrameworkConstants.pollingInterval())
				.ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class)
				.ignoring(ElementClickInterceptedException.class)
				.withMessage(description)
				.until(condition);
	}

	public static <T> T fluentlyUntil(Function<WebDriver, T> condition, String description) {
		return fluentlyUntil(condition, FrameworkConstants.explicitWait(), description);
	}

	/** Blocks until {@code document.readyState} is {@code complete}. */
	public static void waitForPageLoad() {
		fluentlyUntil(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")),
				"Page did not finish loading");
	}

	public static void waitForUrlContains(String fragment) {
		wait().until(ExpectedConditions.urlContains(fragment));
	}

	public static void waitForTitleContains(String fragment) {
		wait().until(ExpectedConditions.titleContains(fragment));
	}

	public static void waitForTextIn(By locator, String text) {
		wait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
	}

	public static void waitForElementCount(By locator, int expected) {
		wait().until(ExpectedConditions.numberOfElementsToBe(locator, expected));
	}

	/** Waits for a spinner/overlay to disappear; returns quietly if it never appeared. */
	public static void waitForInvisibility(By locator) {
		wait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}

	public static void waitForAttributeValue(By locator, String attribute, String value) {
		wait().until(ExpectedConditions.attributeToBe(locator, attribute, value));
	}
}
