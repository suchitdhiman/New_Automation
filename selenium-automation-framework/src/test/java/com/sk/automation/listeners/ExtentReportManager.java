package com.sk.automation.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Owns the ExtentReports instance and the per-thread {@link ExtentTest}.
 *
 * <p>One reporter for the whole suite, one test node per thread. Without the
 * {@link ThreadLocal}, parallel tests write their log lines into each other's
 * report sections and the output is unusable.
 *
 * <p>Uses {@code ExtentSparkReporter}; {@code ExtentHtmlReporter} was removed in
 * ExtentReports 5.
 */
public final class ExtentReportManager {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private static ExtentReports extentReports;

    private ExtentReportManager() {
        // Utility class — no instances.
    }

    public static synchronized ExtentReports getReporter() {
        if (extentReports == null) {
            extentReports = buildReporter();
        }
        return extentReports;
    }

    private static ExtentReports buildReporter() {
        Path reportPath = Path.of(System.getProperty("user.dir"), "target", "reports",
                "ExtentReport_" + LocalDateTime.now().format(TIMESTAMP) + ".html");

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath.toFile());
        spark.config().setDocumentTitle("UI Automation Report");
        spark.config().setReportName("Selenium Regression Suite");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setEncoding("utf-8");
        spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        reports.setSystemInfo("Operating System", System.getProperty("os.name"));
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("Browser", System.getProperty("browser", "chrome"));
        reports.setSystemInfo("Environment", System.getProperty("env", "qa"));
        reports.setSystemInfo("Executed By", System.getProperty("user.name"));
        return reports;
    }

    public static void startTest(String name, String description) {
        CURRENT_TEST.set(getReporter().createTest(name, description));
    }

    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static void removeTest() {
        CURRENT_TEST.remove();
    }

    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
