package com.sk.webdriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {
	public static ExtentHtmlReporter extentHtmlReporter;
	public static ExtentReports extentReporters;

	public static ExtentReports getInstance() {
		if (extentHtmlReporter == null) {
			extentHtmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "//Report//htmlReport.html");
			extentHtmlReporter.config().setDocumentTitle("Automation Testing");
			extentHtmlReporter.config().setTheme(Theme.STANDARD);
			extentReporters = new ExtentReports();
			extentReporters.attachReporter(extentHtmlReporter);
			extentReporters.setSystemInfo("os", System.getProperty("os.name"));
			extentReporters.setSystemInfo("osversion", System.getProperty("os.version"));
			extentReporters.setSystemInfo("Tester Name", System.getProperty("user.name"));
		}
		return extentReporters;
	}
}