package com.sk.automation.listeners;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.sk.automation.utils.ScreenshotUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Wires reporting into the TestNG lifecycle.
 *
 * <p>This is what keeps reporting out of the tests themselves. A test method describes
 * a scenario and asserts an outcome; capturing a screenshot and writing a FAIL entry
 * is infrastructure, and infrastructure belongs in a listener. Adding a new test
 * requires no reporting code at all.
 */
public class TestListener implements ITestListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        LOG.info("===== Suite started: {} =====", context.getName());
        ExtentReportManager.getReporter();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        LOG.info("----- Test started: {} -----", name);
        ExtentReportManager.startTest(name, description == null ? "" : description);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("Test passed: {}", result.getMethod().getMethodName());
        ExtentReportManager.getTest().log(Status.PASS, "Test passed");
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = result.getMethod().getMethodName();
        LOG.error("Test failed: {}", name, result.getThrowable());

        ExtentReportManager.getTest().log(Status.FAIL, result.getThrowable());

        ScreenshotUtil.capture(name).ifPresent(screenshot ->
                ExtentReportManager.getTest().fail("Screenshot at point of failure",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot.base64()).build()));

        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("Test skipped: {}", result.getMethod().getMethodName());
        if (ExtentReportManager.getTest() != null) {
            ExtentReportManager.getTest().log(Status.SKIP, "Test skipped");
            ExtentReportManager.removeTest();
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        LOG.info("===== Suite finished: {} =====", context.getName());
        // Without this flush the HTML file exists but stays empty.
        ExtentReportManager.flush();
    }
}
