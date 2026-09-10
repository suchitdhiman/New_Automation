package com.sk.ecom.pages.base;

import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.driver.DriverManager;
import com.sk.ecom.enums.WaitStrategy;
import com.sk.ecom.exceptions.ElementInteractionException;
import com.sk.ecom.logging.Log;
import com.sk.ecom.utils.JavaScriptUtils;
import com.sk.ecom.utils.WaitFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * The interaction engine every page object inherits.
 *
 * <p>Design rules this class exists to enforce:
 *
 * <ul>
 *   <li><b>No driver field.</b> {@link #driver()} resolves the thread's driver
 *       on every call, so a page object can never leak a session across
 *       scenarios.</li>
 *   <li><b>Every interaction waits.</b> Callers pass intent
 *       ({@link WaitStrategy}), not durations.</li>
 *   <li><b>Every interaction is named.</b> The business name of the element goes
 *       into the log, the Extent report and the failure message, so a red build
 *       says "Failed to click on [Checkout button]" rather than dumping an
 *       XPath.</li>
 *   <li><b>Self-healing where it is honest.</b> A click that is intercepted is
 *       retried after scrolling, and only then falls back to a JavaScript click
 *       — and it says in the log that it did, so a genuinely broken UI is never
 *       silently papered over.</li>
 * </ul>
 */
public abstract class BasePage {

	private static final Duration QUICK_CHECK = Duration.ofSeconds(3);

	/** Each page declares the landmark that proves it is on screen. */
	public abstract boolean isAt();

	protected WebDriver driver() {
		return DriverManager.getDriver();
	}

	/* ------------------------------------------------------------------ */
	/* Location and navigation                                             */
	/* ------------------------------------------------------------------ */

	protected void openUrl(String url) {
		driver().get(url);
		WaitFactory.waitForPageLoad();
		Log.step("Opened URL: %s", url);
	}

	public String currentUrl() {
		return driver().getCurrentUrl();
	}

	public String pageTitle() {
		return driver().getTitle();
	}

	/* ------------------------------------------------------------------ */
	/* Element lookup                                                      */
	/* ------------------------------------------------------------------ */

	protected WebElement find(By locator) {
		return find(locator, WaitStrategy.VISIBLE);
	}

	protected WebElement find(By locator, WaitStrategy strategy) {
		return WaitFactory.waitFor(locator, strategy);
	}

	protected List<WebElement> findAll(By locator) {
		return driver().findElements(locator);
	}

	protected int countOf(By locator) {
		return findAll(locator).size();
	}

	/* ------------------------------------------------------------------ */
	/* Clicking                                                            */
	/* ------------------------------------------------------------------ */

	protected void click(By locator, String elementName) {
		click(locator, elementName, WaitStrategy.CLICKABLE);
	}

	protected void click(By locator, String elementName, WaitStrategy strategy) {
		try {
			WebElement element = find(locator, strategy);
			highlightIfEnabled(element);
			try {
				element.click();
			} catch (ElementClickInterceptedException | StaleElementReferenceException
					| ElementNotInteractableException first) {
				Log.warn("Direct click on [" + elementName + "] failed with "
						+ first.getClass().getSimpleName() + "; scrolling and retrying");
				WebElement refreshed = find(locator, WaitStrategy.PRESENCE);
				JavaScriptUtils.scrollIntoView(refreshed);
				try {
					find(locator, WaitStrategy.CLICKABLE).click();
				} catch (RuntimeException second) {
					Log.warnStep("Falling back to a JavaScript click on [" + elementName
							+ "] - verify this element is genuinely reachable by a user");
					JavaScriptUtils.click(find(locator, WaitStrategy.PRESENCE));
				}
			}
			Log.step("Clicked on [%s]", elementName);
		} catch (RuntimeException e) {
			throw new ElementInteractionException("click", elementName, locator, e);
		}
	}

	/** Explicit JS click, for the rare element that is legitimately off-screen. */
	protected void jsClick(By locator, String elementName) {
		try {
			JavaScriptUtils.click(find(locator, WaitStrategy.PRESENCE));
			Log.step("JavaScript-clicked on [%s]", elementName);
		} catch (RuntimeException e) {
			throw new ElementInteractionException("javascript click", elementName, locator, e);
		}
	}

	/* ------------------------------------------------------------------ */
	/* Typing                                                              */
	/* ------------------------------------------------------------------ */

	protected void type(By locator, String text, String elementName) {
		try {
			WebElement element = find(locator, WaitStrategy.VISIBLE);
			highlightIfEnabled(element);
			element.clear();
			element.sendKeys(text);
			Log.step("Entered [%s] into [%s]", mask(elementName, text), elementName);
		} catch (RuntimeException e) {
			throw new ElementInteractionException("type into", elementName, locator, e);
		}
	}

	/** Clears via keyboard, for inputs whose framework ignores {@code clear()}. */
	protected void clearWithKeys(By locator, String elementName) {
		WebElement element = find(locator, WaitStrategy.VISIBLE);
		element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
		Log.step("Cleared [%s]", elementName);
	}

	protected void pressKey(By locator, Keys key, String elementName) {
		find(locator, WaitStrategy.VISIBLE).sendKeys(key);
		Log.step("Pressed [%s] on [%s]", key.name(), elementName);
	}

	/** Sets an {@code <input type=file>} value; works on Grid via LocalFileDetector. */
	protected void uploadFile(By locator, String absolutePath, String elementName) {
		try {
			find(locator, WaitStrategy.PRESENCE).sendKeys(absolutePath);
			Log.step("Uploaded [%s] via [%s]", absolutePath, elementName);
		} catch (RuntimeException e) {
			throw new ElementInteractionException("upload a file to", elementName, locator, e);
		}
	}

	/* ------------------------------------------------------------------ */
	/* Reading                                                             */
	/* ------------------------------------------------------------------ */

	protected String textOf(By locator, String elementName) {
		try {
			String text = find(locator, WaitStrategy.VISIBLE).getText().trim();
			Log.debug("Read [" + text + "] from [" + elementName + "]");
			return text;
		} catch (RuntimeException e) {
			throw new ElementInteractionException("read text from", elementName, locator, e);
		}
	}

	protected List<String> textsOf(By locator) {
		return findAll(locator).stream()
				.map(WebElement::getText)
				.map(String::trim)
				.collect(Collectors.toList());
	}

	protected String attributeOf(By locator, String attribute, String elementName) {
		try {
			return find(locator, WaitStrategy.PRESENCE).getDomAttribute(attribute);
		} catch (RuntimeException e) {
			throw new ElementInteractionException("read attribute [" + attribute + "] from", elementName, locator, e);
		}
	}

	protected String valueOf(By locator, String elementName) {
		return find(locator, WaitStrategy.PRESENCE).getDomProperty("value");
	}

	protected String cssValueOf(By locator, String property) {
		return find(locator, WaitStrategy.PRESENCE).getCssValue(property);
	}

	/* ------------------------------------------------------------------ */
	/* State checks                                                        */
	/* ------------------------------------------------------------------ */

	/** Short-circuit check: never throws, so it is safe inside an assertion. */
	protected boolean isDisplayed(By locator) {
		return isDisplayed(locator, QUICK_CHECK);
	}

	protected boolean isDisplayed(By locator, Duration timeout) {
		try {
			return WaitFactory.wait(timeout)
					.until(d -> {
						List<WebElement> elements = d.findElements(locator);
						return !elements.isEmpty() && elements.get(0).isDisplayed();
					});
		} catch (TimeoutException e) {
			return false;
		}
	}

	protected boolean isAbsent(By locator) {
		try {
			WaitFactory.wait(QUICK_CHECK).until(d -> d.findElements(locator).isEmpty()
					|| !d.findElements(locator).get(0).isDisplayed());
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	protected boolean isEnabled(By locator) {
		return find(locator, WaitStrategy.PRESENCE).isEnabled();
	}

	protected boolean isSelected(By locator) {
		return find(locator, WaitStrategy.PRESENCE).isSelected();
	}

	/** Idempotent: only clicks when the current state differs from the target. */
	protected void setCheckbox(By locator, boolean checked, String elementName) {
		WebElement element = find(locator, WaitStrategy.CLICKABLE);
		if (element.isSelected() != checked) {
			element.click();
			Log.step("Set [%s] to %s", elementName, checked ? "checked" : "unchecked");
		} else {
			Log.debug("[" + elementName + "] was already " + (checked ? "checked" : "unchecked"));
		}
	}

	/* ------------------------------------------------------------------ */
	/* Native select                                                       */
	/* ------------------------------------------------------------------ */

	protected Select selectOf(By locator) {
		return new Select(find(locator, WaitStrategy.VISIBLE));
	}

	protected void selectByVisibleText(By locator, String text, String elementName) {
		selectOf(locator).selectByVisibleText(text);
		Log.step("Selected [%s] in [%s]", text, elementName);
	}

	protected void selectByValue(By locator, String value, String elementName) {
		selectOf(locator).selectByValue(value);
		Log.step("Selected value [%s] in [%s]", value, elementName);
	}

	protected void selectByIndex(By locator, int index, String elementName) {
		selectOf(locator).selectByIndex(index);
		Log.step("Selected index [%d] in [%s]", index, elementName);
	}

	protected String selectedOption(By locator) {
		return selectOf(locator).getFirstSelectedOption().getText().trim();
	}

	protected List<String> dropdownOptions(By locator) {
		return selectOf(locator).getOptions().stream()
				.map(WebElement::getText)
				.map(String::trim)
				.collect(Collectors.toList());
	}

	/* ------------------------------------------------------------------ */
	/* Mouse and keyboard                                                  */
	/* ------------------------------------------------------------------ */

	protected Actions actions() {
		return new Actions(driver());
	}

	protected void hoverOver(By locator, String elementName) {
		WebElement element = find(locator, WaitStrategy.VISIBLE);
		JavaScriptUtils.scrollIntoView(element);
		actions().moveToElement(element).perform();
		Log.step("Hovered over [%s]", elementName);
	}

	protected void doubleClick(By locator, String elementName) {
		actions().doubleClick(find(locator, WaitStrategy.CLICKABLE)).perform();
		Log.step("Double-clicked [%s]", elementName);
	}

	protected void rightClick(By locator, String elementName) {
		actions().contextClick(find(locator, WaitStrategy.CLICKABLE)).perform();
		Log.step("Right-clicked [%s]", elementName);
	}

	protected void dragAndDrop(By source, By target, String description) {
		WebElement from = find(source, WaitStrategy.VISIBLE);
		WebElement to = find(target, WaitStrategy.VISIBLE);
		actions().clickAndHold(from).moveToElement(to).release().perform();
		Log.step("Dragged %s", description);
	}

	protected void dragByOffset(By source, int xOffset, int yOffset, String elementName) {
		actions().dragAndDropBy(find(source, WaitStrategy.VISIBLE), xOffset, yOffset).perform();
		Log.step("Dragged [%s] by (%d, %d)", elementName, xOffset, yOffset);
	}

	protected void scrollTo(By locator) {
		JavaScriptUtils.scrollIntoView(find(locator, WaitStrategy.PRESENCE));
	}

	/* ------------------------------------------------------------------ */
	/* Internals                                                           */
	/* ------------------------------------------------------------------ */

	private void highlightIfEnabled(WebElement element) {
		if (FrameworkConstants.highlightElements()) {
			try {
				JavaScriptUtils.highlight(element);
			} catch (RuntimeException e) {
				Log.debug("Highlighting skipped: " + e.getMessage());
			}
		}
	}

	/** Keeps credentials out of the log file and the HTML report. */
	private String mask(String elementName, String value) {
		String name = elementName == null ? "" : elementName.toLowerCase(Locale.ROOT);
		boolean secret = name.contains("password") || name.contains("secret") || name.contains("token")
				|| name.contains("card");
		return secret ? "********" : value;
	}
}
