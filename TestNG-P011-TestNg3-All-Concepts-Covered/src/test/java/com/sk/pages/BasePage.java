package com.sk.pages;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sk.core.ConfigManager;

/**
 * Shared plumbing for the page objects: waits, clicks, typing, reading text.
 *
 * <p>Two waits on purpose. {@link #wait} is the normal one (15s by default) and
 * is used whenever we expect something to appear. {@link #shortWait} is a
 * couple of seconds and is used only by {@link #isPresent(By)}, because a
 * negative check ("the error banner should NOT be there") that waits the full
 * 15 seconds turns a fast suite into a slow one.
 *
 * <p>Implicit wait is left at 0 in {@code config.properties}. Mixing implicit
 * and explicit waits makes the effective timeout unpredictable - Selenium
 * documents this and it is worth not relearning it the hard way.
 *
 * <p>No {@code PageFactory}/{@code @FindBy} here. {@code By} constants are
 * easier to build dynamically (see the "add to cart button for product X"
 * locators in {@link ProductsPage}) and they do not go stale the way proxied
 * {@code WebElement} fields do.
 */
public abstract class BasePage {

	protected final Logger log = LogManager.getLogger(getClass());

	/** How many times {@link #type(By, String)} will retype a field before giving up. */
	private static final int TYPE_ATTEMPTS = 3;

	protected final WebDriver driver;
	protected final WebDriverWait wait;
	protected final WebDriverWait shortWait;

	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, ConfigManager.getSeconds("explicit.wait"));
		this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
	}

	/**
	 * Every page answers this. Test code calls it instead of sprinkling
	 * {@code Thread.sleep} around after a navigation.
	 */
	public abstract boolean isLoaded();

	// -- interactions --------------------------------------------------------

	protected WebElement visible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	protected WebElement clickable(By locator) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	protected void click(By locator) {
		clickable(locator).click();
	}

	/**
	 * Types into a field and then <b>checks what actually landed there</b>,
	 * retrying if the two do not match.
	 *
	 * <p>This is not defensive padding. Swag Labs is a React app and its inputs
	 * are controlled components: every keystroke goes through an onChange handler
	 * that rewrites the value. Under load - three headless Chromes on one box
	 * during a {@code parallel="methods"} run - that handler falls behind and
	 * characters get dropped. The symptom is a login that types
	 * {@code standard_user} and submits {@code standard_use}, and an error banner
	 * saying the credentials do not match. Intermittent, unreproducible by hand,
	 * and it looks exactly like a product bug.
	 *
	 * <p>{@code getDomProperty("value")} reads the live DOM property rather than
	 * the static HTML attribute, which is the only way to see what an input
	 * really holds after scripted typing.
	 *
	 * <p>Fixing it here means no test and no page object has to know about it.
	 * The alternative - a retry analyzer on every test that logs in - would hide
	 * the problem instead of solving it.
	 */
	protected void type(By locator, String text) {
		String expected = (text == null) ? "" : text;

		for (int attempt = 1; attempt <= TYPE_ATTEMPTS; attempt++) {
			WebElement field = visible(locator);
			field.clear();
			if (!expected.isEmpty()) {
				field.sendKeys(expected);
			}

			String actual = valueOf(field);
			if (expected.equals(actual)) {
				return;
			}
			log.warn("Field {} holds [{}] after typing [{}] - retrying ({}/{})",
					locator, actual, expected, attempt, TYPE_ATTEMPTS);
		}

		throw new IllegalStateException("Could not reliably type [" + expected + "] into " + locator
				+ " after " + TYPE_ATTEMPTS + " attempts. The page is dropping keystrokes.");
	}

	private String valueOf(WebElement field) {
		String value = field.getDomProperty("value");
		return value == null ? "" : value;
	}

	protected String textOf(By locator) {
		return visible(locator).getText().trim();
	}

	protected List<String> textsOf(By locator) {
		return driver.findElements(locator).stream()
				.map(WebElement::getText)
				.map(String::trim)
				.toList();
	}

	protected int countOf(By locator) {
		return driver.findElements(locator).size();
	}

	protected void selectByValue(By locator, String value) {
		new Select(visible(locator)).selectByValue(value);
	}

	protected String selectedLabel(By locator) {
		return new Select(visible(locator)).getFirstSelectedOption().getText().trim();
	}

	/**
	 * Short-wait presence check that answers false instead of throwing. Use it
	 * for assertions about what should NOT be on screen.
	 */
	protected boolean isPresent(By locator) {
		try {
			return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
		} catch (TimeoutException | NoSuchElementException e) {
			return false;
		}
	}

	// -- page level ----------------------------------------------------------

	public String currentUrl() {
		return driver.getCurrentUrl();
	}

	public String pageTitle() {
		return driver.getTitle();
	}

	/** Fails fast with a message that names the page, which beats a bare TimeoutException. */
	protected void assertLoaded(By anchor) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(anchor));
		} catch (TimeoutException e) {
			throw new IllegalStateException(getClass().getSimpleName() + " did not load within "
					+ ConfigManager.getInt("explicit.wait") + "s. Current URL: " + currentUrl(), e);
		}
	}
}
