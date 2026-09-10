package com.sk.ecom.driver;

import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.enums.BrowserType;
import com.sk.ecom.enums.ConfigKey;
import com.sk.ecom.enums.TargetType;
import com.sk.ecom.exceptions.FrameworkException;
import com.sk.ecom.logging.Log;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Creates and configures a browser session.
 *
 * <p>Driver binaries are resolved by Selenium Manager, which ships inside
 * Selenium 4.x — there is deliberately no WebDriverManager dependency and no
 * checked-in {@code .exe}.
 *
 * <p>The returned driver is wrapped in an {@code EventFiringDecorator} so that
 * logging is applied uniformly rather than sprinkled through page objects.
 */
public final class DriverFactory {

	private DriverFactory() {
		throw new IllegalStateException("Utility class");
	}

	/** Builds a driver from the active configuration and binds it to this thread. */
	public static WebDriver createDriver() {
		BrowserType browser = BrowserType.from(ConfigManager.get(ConfigKey.BROWSER, "chrome"));
		TargetType target = TargetType.from(ConfigManager.get(ConfigKey.TARGET, "local"));
		return createDriver(browser, target);
	}

	public static WebDriver createDriver(BrowserType browser, TargetType target) {
		Log.info("Starting " + browser + " on " + target + " target [env=" + ConfigManager.environment()
				+ ", headless=" + ConfigManager.get(ConfigKey.HEADLESS, "false") + "]");

		WebDriver raw = target == TargetType.REMOTE ? remote(browser) : local(browser);
		WebDriver driver = new EventFiringDecorator<WebDriver>(new WebDriverLogListener()).decorate(raw);
		applySessionSettings(driver);
		return driver;
	}

	/* ------------------------------------------------------------------ */

	private static WebDriver local(BrowserType browser) {
		MutableCapabilities capabilities = BrowserOptionsManager.build(browser);
		return switch (browser) {
			case CHROME -> new ChromeDriver((ChromeOptions) capabilities);
			case FIREFOX -> new FirefoxDriver((FirefoxOptions) capabilities);
			case EDGE -> new EdgeDriver((EdgeOptions) capabilities);
			case SAFARI -> new SafariDriver((SafariOptions) capabilities);
		};
	}

	private static WebDriver remote(BrowserType browser) {
		String gridUrl = ConfigManager.get(ConfigKey.GRID_URL, "http://localhost:4444");
		RemoteWebDriver driver = new RemoteWebDriver(toUrl(gridUrl), BrowserOptionsManager.build(browser));
		// Without this, uploading a file to a Grid node silently fails because
		// the path only exists on the machine running the test.
		driver.setFileDetector(new LocalFileDetector());
		return driver;
	}

	private static URL toUrl(String gridUrl) {
		try {
			return URI.create(gridUrl).toURL();
		} catch (MalformedURLException | IllegalArgumentException e) {
			throw new FrameworkException("Invalid grid.url [" + gridUrl + "]", e);
		}
	}

	private static void applySessionSettings(WebDriver driver) {
		driver.manage().timeouts().pageLoadTimeout(FrameworkConstants.pageLoadTimeout());
		driver.manage().timeouts().scriptTimeout(FrameworkConstants.scriptTimeout());

		// Implicit wait defaults to 0. Mixing implicit and explicit waits makes
		// timeouts unpredictable, so this stays off unless deliberately enabled.
		if (!FrameworkConstants.implicitWait().isZero()) {
			driver.manage().timeouts().implicitlyWait(FrameworkConstants.implicitWait());
			Log.warn("Implicit wait is enabled; prefer explicit waits via WaitStrategy.");
		}

		if (ConfigManager.getBoolean(ConfigKey.BROWSER_MAXIMIZE) && !ConfigManager.getBoolean(ConfigKey.HEADLESS)) {
			driver.manage().window().maximize();
		}
		if (ConfigManager.getBoolean(ConfigKey.DELETE_COOKIES)) {
			driver.manage().deleteAllCookies();
		}
	}
}
