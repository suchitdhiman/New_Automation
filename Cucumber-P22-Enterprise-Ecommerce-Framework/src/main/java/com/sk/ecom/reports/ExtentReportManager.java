package com.sk.ecom.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.enums.ConfigKey;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Owns the single {@link ExtentReports} instance for the whole JVM.
 *
 * <p>There is exactly one report per run no matter how many scenario threads
 * are alive; per-thread state lives in {@link ExtentTestManager}. Creation and
 * flushing are driven by {@link ExtentCucumberPlugin} from Cucumber run events,
 * so the report is correct whether you launch from Maven, from a TestNG suite
 * or from a single feature file in the IDE.
 */
public final class ExtentReportManager {

	private static ExtentReports extentReports;

	private ExtentReportManager() {
		throw new IllegalStateException("Utility class");
	}

	public static synchronized ExtentReports getInstance() {
		if (extentReports == null) {
			extentReports = build();
		}
		return extentReports;
	}

	private static ExtentReports build() {
		new File(FrameworkConstants.REPORT_DIR).mkdirs();
		new File(FrameworkConstants.SCREENSHOT_DIR).mkdirs();

		ExtentSparkReporter spark = new ExtentSparkReporter(FrameworkConstants.EXTENT_REPORT_FILE);
		spark.config().setTheme(resolveTheme());
		spark.config().setDocumentTitle(ConfigManager.get(ConfigKey.REPORT_TITLE, "Automation Report"));
		spark.config().setReportName(ConfigManager.get(ConfigKey.REPORT_NAME, "E-Commerce BDD Regression"));
		spark.config().setTimeStampFormat("dd-MMM-yyyy HH:mm:ss");
		spark.config().setEncoding("utf-8");

		ExtentReports reports = new ExtentReports();
		reports.attachReporter(spark);
		attachSystemInfo(reports);
		return reports;
	}

	private static Theme resolveTheme() {
		String configured = ConfigManager.get(ConfigKey.REPORT_THEME, "STANDARD").toUpperCase(Locale.ROOT);
		return "DARK".equals(configured) ? Theme.DARK : Theme.STANDARD;
	}

	/** Everything a reader needs to reproduce the run. */
	private static void attachSystemInfo(ExtentReports reports) {
		reports.setSystemInfo("Environment", ConfigManager.environment().toUpperCase(Locale.ROOT));
		reports.setSystemInfo("Application", ConfigManager.get(ConfigKey.APP_BASE_URL, "n/a"));
		reports.setSystemInfo("Browser", ConfigManager.get(ConfigKey.BROWSER, "chrome"));
		reports.setSystemInfo("Execution Target", ConfigManager.get(ConfigKey.TARGET, "local"));
		reports.setSystemInfo("Headless", ConfigManager.get(ConfigKey.HEADLESS, "false"));
		reports.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		reports.setSystemInfo("Java", System.getProperty("java.version"));
		reports.setSystemInfo("User", System.getProperty("user.name"));
		reports.setSystemInfo("Host", hostName());
	}

	private static String hostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "unknown";
		}
	}

	public static synchronized void flush() {
		if (extentReports != null) {
			extentReports.flush();
		}
	}
}
