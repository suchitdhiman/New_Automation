package testngpack.base;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import testngpack.listeners.SuiteListener;
import testngpack.listeners.TestListener;
import testngpack.utils.Log;

/**
 * Base for every test class.
 *
 * <p>What it demonstrates:
 * <ul>
 * <li>{@code @Parameters} - values pushed in from the suite xml
 * ({@code <parameter name="browser" value="chrome"/>}).</li>
 * <li>{@code @Optional} - a fallback used when the suite xml does not declare
 * that parameter, so the same class runs from any suite and from a plain
 * Eclipse "Run as TestNG Test".</li>
 * <li>Thread confinement - the driver comes from {@link DriverFactory}, never
 * from a static field, which is what makes parallel="methods|classes|tests|
 * instances" safe.</li>
 * </ul>
 */
@Listeners({ TestListener.class, SuiteListener.class })
public class BaseClass {

	/** Sets a &lt;select&gt; from the DOM when Selenium refuses to touch it. */
	private static final String SELECT_BY_TEXT_JS = """
			const sel = arguments[0], text = arguments[1];
			const opts = Array.from(sel.options);
			const opt = opts.find(o => o.text.trim() === text)
			         || opts.find(o => o.text.trim().startsWith(text));
			if (!opt) {
			  throw new Error("No option matching: " + text
			    + " | available: " + opts.map(o => o.text.trim()).join(", "));
			}
			sel.value = opt.value;
			sel.dispatchEvent(new Event('change', { bubbles: true }));
			""";

	@BeforeSuite(alwaysRun = true)
	public void loadConfiguration() {
		// Idempotent: safe even though every subclass inherits this method.
		ConfigReader.init();
		ExtentManager.getInstance();
		Log.info("Configuration + Extent report initialised");
	}

	/**
	 * @param browser  from the suite xml; falls back to chrome when absent.
	 * @param urlKey   logical key from url.properties; falls back to amazon.
	 * @param headless from the suite xml or -Dheadless; falls back to false.
	 */
	@Parameters({ "browser", "url", "headless" })
	@BeforeMethod(alwaysRun = true)
	public void setUp(@Optional("chrome") String browser,
			@Optional("amazon") String urlKey,
			@Optional("false") String headless,
			Method method) {

		// A -Dbrowser=edge on the command line beats the xml value.
		String resolvedBrowser = System.getProperty("browser", browser);
		boolean resolvedHeadless = Boolean.parseBoolean(System.getProperty("headless", headless));

		ExtentManager.createTest(method.getDeclaringClass().getSimpleName() + "." + method.getName(),
				"browser=" + resolvedBrowser + " | url=" + urlKey);

		DriverFactory.createDriver(resolvedBrowser, resolvedHeadless);
		openUrl(urlKey);
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		DriverFactory.quitDriver();
		ExtentManager.unload();
	}

	// ------------------------------------------------------------------
	// Convenience accessors
	// ------------------------------------------------------------------

	protected WebDriver driver() {
		return DriverFactory.getDriver();
	}

	/**
	 * Reads a suite/test level parameter at runtime with a default - the
	 * programmatic twin of {@code @Optional}, handy inside @DataProvider or
	 * when the value is only needed on some code paths.
	 */
	protected static String optionalParam(ITestContext context, String key, String fallback) {
		String value = context.getCurrentXmlTest().getParameter(key);
		return (value == null || value.trim().isEmpty()) ? fallback : value.trim();
	}

	// ------------------------------------------------------------------
	// Page interaction helpers (same names as before, now thread safe)
	// ------------------------------------------------------------------

	public void openUrl(String urlKey) {
		String url = ConfigReader.url(urlKey);
		driver().get(url);
		Log.info("Opened " + urlKey + " -> " + url);
		ExtentManager.getTest().info("Opened url [" + urlKey + "] " + url);
	}

	/** Picks a value from a &lt;select&gt; dropdown by its visible text. */
	public void selectOption(String locatorKey, String visibleText) {
		WebElement element = findElement(locatorKey);
		try {
			new Select(element).selectByVisibleText(visibleText);
		} catch (UnsupportedOperationException | ElementNotInteractableException e) {
			// Amazon's category box is a real <select> hidden behind a styled
			// widget, and Selenium refuses to drive an invisible select. Fall
			// back to setting the value in the DOM and firing 'change'.
			selectViaJavascript(element, visibleText);
		}
		Log.info("Selected <" + visibleText + "> on " + locatorKey);
		ExtentManager.getTest().pass("Selected &lt;" + visibleText + "&gt; using " + ConfigReader.locator(locatorKey));
	}

	private void selectViaJavascript(WebElement select, String visibleText) {
		((JavascriptExecutor) driver()).executeScript(SELECT_BY_TEXT_JS, select, visibleText);
	}

	public void textType(String locatorKey, String text) {
		WebElement element = findElement(locatorKey);
		element.clear();
		element.sendKeys(text);
		Log.info("Typed <" + text + "> into " + locatorKey);
		ExtentManager.getTest().pass("Typed &lt;" + text + "&gt; into " + ConfigReader.locator(locatorKey));
	}

	public void clickElement(String locatorKey) {
		findElement(locatorKey).click();
		Log.info("Clicked " + locatorKey);
		ExtentManager.getTest().pass("Clicked " + ConfigReader.locator(locatorKey));
	}

	public String getText(String locatorKey) {
		return findElement(locatorKey).getText();
	}

	/**
	 * Waits until the document actually has a title before returning it.
	 * Without the wait, a fast assertion right after {@code get()} can read an
	 * empty string and fail for no real reason - a classic parallel-run flake.
	 */
	public String getTitle() {
		try {
			DriverFactory.getWait().until(d -> d.getTitle() != null && !d.getTitle().trim().isEmpty());
		} catch (Exception e) {
			Log.warn("Page title still blank after the explicit wait");
		}
		return driver().getTitle();
	}

	public WebElement findElement(String locatorKey) {
		if (!isElementPresent(locatorKey)) {
			throw new org.openqa.selenium.NoSuchElementException(
					"Element '" + locatorKey + "' (" + ConfigReader.locator(locatorKey) + ") not present in time");
		}
		return driver().findElement(getLocator(locatorKey));
	}

	public boolean isElementPresent(String locatorKey) {
		try {
			DriverFactory.getWait().until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorKey)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Locator strategy is derived from the suffix of the key in or.properties. */
	public By getLocator(String locatorKey) {
		String value = ConfigReader.locator(locatorKey);
		if (locatorKey.endsWith("_id")) {
			return By.id(value);
		} else if (locatorKey.endsWith("_name")) {
			return By.name(value);
		} else if (locatorKey.endsWith("_className")) {
			return By.className(value);
		} else if (locatorKey.endsWith("_linkText")) {
			return By.linkText(value);
		} else if (locatorKey.endsWith("_xPath")) {
			return By.xpath(value);
		} else if (locatorKey.endsWith("_cssSelector")) {
			return By.cssSelector(value);
		} else if (locatorKey.endsWith("_tagName")) {
			return By.tagName(value);
		} else if (locatorKey.endsWith("_partialLinkText")) {
			return By.partialLinkText(value);
		}
		throw new IllegalArgumentException("Locator key '" + locatorKey + "' has no recognised suffix "
				+ "(_id, _name, _className, _linkText, _xPath, _cssSelector, _tagName, _partialLinkText)");
	}

	/** Blocks until the address bar contains {@code fragment}. */
	public void waitForUrlContaining(String fragment) {
		DriverFactory.getWait().until(ExpectedConditions.urlContains(fragment));
	}

	public void selectFrame(String frameName) {
		driver().switchTo().frame(driver().findElement(By.name(frameName)));
	}

	// ------------------------------------------------------------------
	// Screenshots
	// ------------------------------------------------------------------

	/** Saves a full page screenshot and returns its absolute path. */
	public static String takeScreenshot(String testName) throws IOException {
		String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		File dir = new File(ConfigReader.PROJECT_DIR, "failurescreenshots");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create screenshot directory: " + dir.getAbsolutePath());
		}

		String safeName = testName.replaceAll("[^A-Za-z0-9._-]", "_");
		File target = new File(dir, safeName + "_" + Thread.currentThread().getName() + "_" + stamp + ".png");

		File source = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.FILE);
		FileHandler.copy(source, target);
		return target.getAbsolutePath();
	}

	public static void drawBorder(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getDriver();
		js.executeScript("arguments[0].style.border='5px solid yellow'", element);
	}
}
