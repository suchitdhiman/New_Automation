package com.sk.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;


/**
 * One ExtentReports instance for the whole suite, one ExtentTest per thread.
 *
 * <p>ExtentReports itself is thread safe; ExtentTest is not, so it lives in a
 * ThreadLocal exactly like the WebDriver does.
 */
public final class ExtentManager {

	private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();
	private static volatile ExtentReports extent;

	private ExtentManager() {
	}

	public static ExtentReports getInstance() {
		if (extent == null) {
			synchronized (ExtentManager.class) {
				if (extent == null) {
					extent = build();
				}
			}
		}
		return extent;
	}

	private static ExtentReports build() {
		ExtentSparkReporter spark = new ExtentSparkReporter(
				ConfigReader.PROJECT_DIR + "/Report/htmlReport.html");
		spark.config().setDocumentTitle("Automation Testing");
		spark.config().setReportName("TestNG Parallel Execution Report");
		spark.config().setTheme(Theme.STANDARD);

		ExtentReports reports = new ExtentReports();
		reports.attachReporter(spark);
		reports.setSystemInfo("os", System.getProperty("os.name"));
		reports.setSystemInfo("osversion", System.getProperty("os.version"));
		reports.setSystemInfo("java", System.getProperty("java.version"));
		reports.setSystemInfo("Tester Name", System.getProperty("user.name"));
		return reports;
	}

	public static ExtentTest createTest(String name, String description) {
		ExtentTest test = getInstance().createTest(name, description);
		TEST.set(test);
		return test;
	}

	/** Never returns null, so tests can log without null checks. */
	public static ExtentTest getTest() {
		ExtentTest test = TEST.get();
		if (test == null) {
			test = createTest(Thread.currentThread().getName(), "Auto-created node");
		}
		return test;
	}

	public static void unload() {
		TEST.remove();
	}

	public static void flush() {
		if (extent != null) {
			extent.flush();
		}
	}
}
