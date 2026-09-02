package testngpack.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.MediaEntityBuilder;

import testngpack.base.BaseClass;
import testngpack.base.DriverFactory;
import testngpack.base.ExtentManager;
import testngpack.utils.Log;

/**
 * Keeps reporting out of the test methods.
 *
 * <p>TestNG fires onTestSuccess/onTestFailure before the @AfterMethod runs, so
 * the browser is still alive here and a failure screenshot can be captured.
 */
public class TestListener implements ITestListener {

	@Override
	public void onTestStart(ITestResult result) {
		Log.info("START  " + name(result) + " on " + DriverFactory.getBrowserName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		Log.info("PASS   " + name(result));
		ExtentManager.getTest().pass("Test passed on " + DriverFactory.getBrowserName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		Log.error("FAIL   " + name(result), result.getThrowable());
		ExtentManager.getTest().fail(result.getThrowable());
		attachScreenshot(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		Log.warn("SKIP   " + name(result));
		ExtentManager.getTest().skip(
				result.getThrowable() == null ? "Skipped" : result.getThrowable().getMessage());
	}

	@Override
	public void onFinish(ITestContext context) {
		Log.info("<test> '" + context.getName() + "' finished: "
				+ context.getPassedTests().size() + " passed, "
				+ context.getFailedTests().size() + " failed, "
				+ context.getSkippedTests().size() + " skipped");
	}

	private void attachScreenshot(ITestResult result) {
		try {
			String path = BaseClass.takeScreenshot(name(result));
			ExtentManager.getTest().fail("Screenshot",
					MediaEntityBuilder.createScreenCaptureFromPath(path).build());
		} catch (Exception e) {
			Log.warn("Could not capture screenshot: " + e.getMessage());
		}
	}

	private String name(ITestResult result) {
		return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
	}
}
