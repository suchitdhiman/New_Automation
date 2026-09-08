package com.sk.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates and hands out one WebDriver <b>per thread</b>.
 *
 * <p>The ThreadLocal is the whole point of this class. The moment a suite says
 * {@code parallel="methods" thread-count="4"}, four test methods run at the
 * same time; one shared static driver would have all four fighting over a
 * single browser window. With a ThreadLocal every thread owns its driver, and
 * the page objects never need to know that parallelism exists at all.
 *
 * <p>{@link #quit()} removes the ThreadLocal entry as well as closing the
 * browser. Surefire reuses worker threads, so a stale entry left behind here
 * leaks into whatever test runs next on that thread.
 *
 * <p>No WebDriverManager dependency: Selenium 4.6+ ships Selenium Manager,
 * which resolves and downloads the matching driver binary by itself.
 */
public final class DriverFactory {

	private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

	private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

	private DriverFactory() {
		// static utility, never instantiated
	}

	public static WebDriver init(Browser browser) {
		if (DRIVER.get() != null) {
			LOG.warn("A driver already exists on thread [{}]. Closing it before starting a new one.",
					Thread.currentThread().getName());
			quit();
		}

		boolean headless = ConfigManager.getBoolean("headless");
		WebDriver driver = switch (browser) {
			case CHROME -> new ChromeDriver(chromeOptions(headless));
			case EDGE -> new EdgeDriver(edgeOptions(headless));
			case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
		};

		driver.manage().timeouts().implicitlyWait(ConfigManager.getSeconds("implicit.wait"));
		driver.manage().timeouts().pageLoadTimeout(ConfigManager.getSeconds("page.load.timeout"));
		if (!headless && ConfigManager.getBoolean("maximize")) {
			driver.manage().window().maximize();
		}

		DRIVER.set(driver);
		LOG.info("Started {} (headless={}) on thread [{}]", browser, headless, Thread.currentThread().getName());
		return driver;
	}

	/**
	 * @throws IllegalStateException when the calling thread has no driver, which
	 *         is a far clearer failure than the NPE you would otherwise get.
	 */
	public static WebDriver getDriver() {
		WebDriver driver = DRIVER.get();
		if (driver == null) {
			throw new IllegalStateException("No WebDriver bound to thread [" + Thread.currentThread().getName()
					+ "]. Does this test class extend BaseWebTest?");
		}
		return driver;
	}

	/** Listeners call this before screenshotting: a non-UI test has no browser to capture. */
	public static boolean hasDriver() {
		return DRIVER.get() != null;
	}

	public static void quit() {
		WebDriver driver = DRIVER.get();
		if (driver == null) {
			return;
		}
		try {
			driver.quit();
		} catch (RuntimeException e) {
			// A browser that already died must not turn a passing test red.
			LOG.warn("driver.quit() failed on thread [{}]: {}", Thread.currentThread().getName(), e.getMessage());
		} finally {
			DRIVER.remove();
		}
	}

	private static ChromeOptions chromeOptions(boolean headless) {
		ChromeOptions options = new ChromeOptions();
		if (headless) {
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		options.addArguments(
				"--disable-gpu",
				"--no-sandbox",
				"--disable-dev-shm-usage",
				"--remote-allow-origins=*",
				"--disable-notifications",
				"--disable-search-engine-choice-screen",
				// Chrome raises a "password found in a data breach" bubble on demo
				// sites, and it steals focus from the very click under test.
				"--disable-features=PasswordLeakDetection,AutofillServerCommunication");
		options.setExperimentalOption("prefs", passwordManagerOff());
		options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
		return options;
	}

	private static EdgeOptions edgeOptions(boolean headless) {
		EdgeOptions options = new EdgeOptions();
		if (headless) {
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		options.addArguments("--disable-gpu", "--no-sandbox", "--disable-notifications");
		options.setExperimentalOption("prefs", passwordManagerOff());
		return options;
	}

	private static FirefoxOptions firefoxOptions(boolean headless) {
		FirefoxOptions options = new FirefoxOptions();
		if (headless) {
			options.addArguments("-headless");
		}
		options.addPreference("dom.webnotifications.enabled", false);
		options.addPreference("signon.rememberSignons", false);
		return options;
	}

	private static Map<String, Object> passwordManagerOff() {
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("profile.password_manager_leak_detection", false);
		return prefs;
	}
}
