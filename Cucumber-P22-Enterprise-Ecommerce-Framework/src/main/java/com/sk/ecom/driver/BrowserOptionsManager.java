package com.sk.ecom.driver;

import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.enums.BrowserType;
import com.sk.ecom.enums.ConfigKey;
import com.sk.ecom.exceptions.InvalidBrowserException;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the capability object for each browser.
 *
 * <p>Kept apart from {@link DriverFactory} so the "what does the browser look
 * like" decision (headless, downloads, hardened flags) is reviewable on its own,
 * and so the same options object can be reused for a local session and for a
 * Grid session without duplication.
 */
public final class BrowserOptionsManager {

	private BrowserOptionsManager() {
		throw new IllegalStateException("Utility class");
	}

	public static MutableCapabilities build(BrowserType browser) {
		return switch (browser) {
			case CHROME -> chrome();
			case FIREFOX -> firefox();
			case EDGE -> edge();
			case SAFARI -> safari();
		};
	}

	private static boolean headless() {
		return ConfigManager.getBoolean(ConfigKey.HEADLESS);
	}

	private static String downloadDir() {
		File dir = new File(FrameworkConstants.DOWNLOAD_DIR);
		dir.mkdirs();
		return dir.getAbsolutePath();
	}

	/* ------------------------------- Chrome ------------------------------- */

	private static ChromeOptions chrome() {
		ChromeOptions options = new ChromeOptions();
		if (headless()) {
			// "=new" is the real Chrome renderer; the legacy headless mode
			// renders differently and produces false negatives on layout.
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		options.addArguments(
				"--start-maximized",
				"--disable-notifications",
				"--disable-popup-blocking",
				"--disable-infobars",
				"--disable-dev-shm-usage",
				"--no-sandbox",
				"--remote-allow-origins=*");
		if (ConfigManager.getBoolean(ConfigKey.BROWSER_INCOGNITO)) {
			options.addArguments("--incognito");
		}

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", downloadDir());
		prefs.put("download.prompt_for_download", false);
		prefs.put("profile.default_content_setting_values.notifications", 2);
		// Chrome's password-breach dialog steals focus mid-test on demo sites.
		prefs.put("credentials_enable_service", false);
		prefs.put("profile.password_manager_enabled", false);
		prefs.put("profile.password_manager_leak_detection", false);
		options.setExperimentalOption("prefs", prefs);
		options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));

		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		options.setAcceptInsecureCerts(true);
		return options;
	}

	/* ------------------------------ Firefox ------------------------------- */

	private static FirefoxOptions firefox() {
		FirefoxOptions options = new FirefoxOptions();
		if (headless()) {
			options.addArguments("-headless", "--width=1920", "--height=1080");
		}
		options.addPreference("browser.download.folderList", 2);
		options.addPreference("browser.download.dir", downloadDir());
		options.addPreference("browser.helperApps.neverAsk.saveToDisk",
				"application/pdf,application/octet-stream,text/csv,application/vnd.ms-excel");
		options.addPreference("pdfjs.disabled", true);
		options.addPreference("dom.webnotifications.enabled", false);
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		options.setAcceptInsecureCerts(true);
		return options;
	}

	/* -------------------------------- Edge -------------------------------- */

	private static EdgeOptions edge() {
		EdgeOptions options = new EdgeOptions();
		if (headless()) {
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		options.addArguments(
				"--start-maximized",
				"--disable-notifications",
				"--disable-popup-blocking",
				"--no-sandbox",
				"--remote-allow-origins=*");
		if (ConfigManager.getBoolean(ConfigKey.BROWSER_INCOGNITO)) {
			options.addArguments("--inprivate");
		}

		Map<String, Object> prefs = new HashMap<>();
		prefs.put("download.default_directory", downloadDir());
		prefs.put("download.prompt_for_download", false);
		options.setExperimentalOption("prefs", prefs);

		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		options.setAcceptInsecureCerts(true);
		return options;
	}

	/* ------------------------------- Safari ------------------------------- */

	private static SafariOptions safari() {
		if (headless()) {
			throw new InvalidBrowserException("Safari has no headless mode. Run with -Dheadless=false.");
		}
		SafariOptions options = new SafariOptions();
		options.setAutomaticInspection(false);
		return options;
	}
}
