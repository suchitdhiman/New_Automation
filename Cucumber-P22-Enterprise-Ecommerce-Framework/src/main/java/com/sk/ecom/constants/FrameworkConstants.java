package com.sk.ecom.constants;

import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.enums.ConfigKey;

import java.time.Duration;

/**
 * Derived, read-only values used across the framework.
 *
 * <p>Nothing here is a magic number: paths are computed from {@code user.dir}
 * so the suite runs identically from Eclipse, from Maven and from a CI agent,
 * and every timeout is sourced from configuration.
 */
public final class FrameworkConstants {

	private FrameworkConstants() {
		throw new IllegalStateException("Utility class");
	}

	/* ------------------------- paths ------------------------- */

	public static final String USER_DIR = System.getProperty("user.dir");
	public static final String TEST_RESOURCES = USER_DIR + "/src/test/resources";
	public static final String TEST_DATA_DIR = TEST_RESOURCES + "/testdata";
	public static final String FEATURES_DIR = TEST_RESOURCES + "/features";

	public static final String OUTPUT_DIR = USER_DIR + "/target";
	public static final String REPORT_DIR = OUTPUT_DIR + "/reports";
	public static final String EXTENT_REPORT_FILE = REPORT_DIR + "/ExtentSparkReport.html";
	public static final String SCREENSHOT_DIR = OUTPUT_DIR + "/screenshots";
	public static final String DOWNLOAD_DIR = OUTPUT_DIR + "/downloads";
	public static final String CUCUMBER_REPORT_DIR = OUTPUT_DIR + "/cucumber-reports";
	public static final String RERUN_FILE = CUCUMBER_REPORT_DIR + "/rerun.txt";

	/* ------------------------ timeouts ------------------------ */

	public static Duration explicitWait() {
		return Duration.ofSeconds(ConfigManager.getInt(ConfigKey.TIMEOUT_EXPLICIT, 20));
	}

	public static Duration pageLoadTimeout() {
		return Duration.ofSeconds(ConfigManager.getInt(ConfigKey.TIMEOUT_PAGE_LOAD, 45));
	}

	public static Duration scriptTimeout() {
		return Duration.ofSeconds(ConfigManager.getInt(ConfigKey.TIMEOUT_SCRIPT, 30));
	}

	public static Duration implicitWait() {
		return Duration.ofSeconds(ConfigManager.getInt(ConfigKey.TIMEOUT_IMPLICIT, 0));
	}

	public static Duration pollingInterval() {
		return Duration.ofMillis(ConfigManager.getInt(ConfigKey.TIMEOUT_POLLING_MILLIS, 300));
	}

	/* ------------------------ behaviour ----------------------- */

	public static int retryCount() {
		return ConfigManager.getInt(ConfigKey.RETRY_COUNT, 0);
	}

	public static boolean screenshotOnFailure() {
		return ConfigManager.getBoolean(ConfigKey.SCREENSHOT_ON_FAILURE);
	}

	public static boolean screenshotOnPass() {
		return ConfigManager.getBoolean(ConfigKey.SCREENSHOT_ON_PASS);
	}

	public static boolean screenshotEveryStep() {
		return ConfigManager.getBoolean(ConfigKey.SCREENSHOT_EVERY_STEP);
	}

	public static boolean highlightElements() {
		return ConfigManager.getBoolean(ConfigKey.HIGHLIGHT_ELEMENTS);
	}
}
