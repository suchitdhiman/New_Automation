package testngpack.base;

import java.time.Duration;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import testngpack.utils.Log;

/**
 * Creates one WebDriver per test thread and hands it out through a ThreadLocal.
 *
 * <p>This is the single change that makes parallel execution possible: with a
 * plain {@code static WebDriver} field, two threads running at the same time
 * overwrite each other's browser and the tests fail at random.
 */
public final class DriverFactory {

	private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
	private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();
	private static final ThreadLocal<String> BROWSER_NAME = new ThreadLocal<>();

	private static final Duration EXPLICIT_WAIT = Duration.ofSeconds(15);
	private static final Duration PAGE_LOAD = Duration.ofSeconds(60);

	private DriverFactory() {
	}

	public static void createDriver(String browser, boolean headless) {
		if (browser == null || browser.trim().isEmpty()) {
			throw new IllegalArgumentException("Browser parameter is null/empty. "
					+ "Pass it via <parameter name=\"browser\" value=\"chrome\"/> in the suite xml.");
		}

		WebDriver driver;
		switch (browser.trim().toLowerCase()) {
		case "chrome":
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--remote-allow-origins=*");
			// EAGER returns from get() at DOMContentLoaded. Ad-heavy sites keep
			// firing requests for a long time after the page is usable, and with
			// the default NORMAL strategy that shows up as a page load timeout.
			chromeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
			if (headless) {
				// Several headless Chromes loading ad-heavy pages at once will
				// crash their renderers without these; they are the standard CI
				// stability flags.
				chromeOptions.addArguments("--headless=new", "--window-size=1920,1080",
						"--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
						"--disable-extensions");
			}
			driver = new ChromeDriver(chromeOptions);
			break;
		case "edge":
			EdgeOptions edgeOptions = new EdgeOptions();
			edgeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
			if (headless) {
				edgeOptions.addArguments("--headless=new", "--window-size=1920,1080",
						"--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
						"--disable-extensions");
			}
			driver = new EdgeDriver(edgeOptions);
			break;
		case "firefox":
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);
			if (headless) {
				firefoxOptions.addArguments("-headless");
			}
			driver = new FirefoxDriver(firefoxOptions);
			break;
		default:
			throw new IllegalArgumentException(
					"Unsupported browser '" + browser + "'. Use chrome, edge or firefox.");
		}

		driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD);
		if (!headless) {
			driver.manage().window().maximize();
		}

		DRIVER.set(driver);
		WAIT.set(new WebDriverWait(driver, EXPLICIT_WAIT));
		BROWSER_NAME.set(browser.trim().toLowerCase());
		Log.info("Launched " + browser + (headless ? " (headless)" : ""));
	}

	public static WebDriver getDriver() {
		WebDriver driver = DRIVER.get();
		if (driver == null) {
			throw new IllegalStateException(
					"No WebDriver for thread '" + Thread.currentThread().getName() + "'. "
							+ "Did the test class extend BaseClass so @BeforeMethod could run?");
		}
		return driver;
	}

	public static WebDriverWait getWait() {
		return WAIT.get();
	}

	public static String getBrowserName() {
		String name = BROWSER_NAME.get();
		return name == null ? "unknown" : name;
	}

	public static void quitDriver() {
		WebDriver driver = DRIVER.get();
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				Log.warn("Ignoring error while quitting driver: " + e.getMessage());
			}
		}
		// Always clear the ThreadLocals: TestNG reuses threads between methods.
		DRIVER.remove();
		WAIT.remove();
		BROWSER_NAME.remove();
	}
}
