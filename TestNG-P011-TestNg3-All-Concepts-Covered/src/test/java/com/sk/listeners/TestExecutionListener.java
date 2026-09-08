package com.sk.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.sk.listeners.ExecutionLedger.Status;
import com.sk.listeners.ExecutionLedger.TestOutcome;

/**
 * {@code ITestListener} - the workhorse. One callback per test method outcome.
 *
 * <p>This is where PASSED / FAILED / SKIPPED is decided and written to the
 * {@link ExecutionLedger}, which every report in this project is built from.
 *
 * <h2>The retry trap</h2>
 * When a retry analyzer says "run it again", TestNG marks the attempt that just
 * failed as <b>SKIP</b> and fires {@link #onTestSkipped}. If you record that
 * blindly, a test that failed twice and passed on the third go reports as
 * "1 passed, 2 skipped" and the numbers stop adding up. {@link
 * ITestResult#wasRetried()} (TestNG 7.5+) is the supported way to tell the two
 * apart, so discarded attempts are logged and dropped here.
 *
 * <h2>Where the screenshot comes from</h2>
 * It is taken in {@code MethodInvocationListener.afterInvocation}, which TestNG
 * calls while the test method has only just returned and the browser is still
 * open. This listener only reads the path back off the result attributes.
 */
public class TestExecutionListener implements ITestListener {

	private static final Logger LOG = LogManager.getLogger(TestExecutionListener.class);

	/** Key shared with MethodInvocationListener. */
	public static final String SCREENSHOT_ATTRIBUTE = "screenshot.path";

	@Override
	public void onStart(ITestContext context) {
		LOG.info("---- <test> START [{}] | included groups: {} | thread-count: {} ----",
				context.getName(),
				String.join(",", context.getIncludedGroups()),
				context.getCurrentXmlTest().getThreadCount());
	}

	@Override
	public void onTestStart(ITestResult result) {
		LOG.info("TEST START  : {}", nameOf(result));
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		LOG.info("TEST PASSED : {} ({} ms)", nameOf(result), durationOf(result));
		record(result, Status.PASSED);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		LOG.error("TEST FAILED : {} ({} ms) | {}", nameOf(result), durationOf(result),
				ExecutionLedger.rootCauseOf(result.getThrowable()));
		record(result, Status.FAILED);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		if (result.wasRetried()) {
			// An attempt that a retry analyzer threw away. Not a skip, do not count it.
			LOG.warn("ATTEMPT DISCARDED (retry pending) : {}", nameOf(result));
			return;
		}
		// A genuine skip: SkipException, an upstream dependsOnMethods failure,
		// or a failed @BeforeMethod.
		LOG.warn("TEST SKIPPED: {} | {}", nameOf(result),
				result.getThrowable() == null ? "dependency not satisfied"
						: ExecutionLedger.rootCauseOf(result.getThrowable()));
		record(result, Status.SKIPPED);
	}

	/**
	 * Fires when a method with {@code successPercentage} failed, but not often
	 * enough to fail the run. TestNG treats it as a pass; so do we, loudly.
	 */
	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		LOG.warn("TEST FAILED BUT WITHIN successPercentage : {}", nameOf(result));
		record(result, Status.PASSED);
	}

	/** Split out from onTestFailure so a {@code timeOut} breach is obvious in the log. */
	@Override
	public void onTestFailedWithTimeout(ITestResult result) {
		LOG.error("TEST TIMED OUT : {} | budget was {} ms",
				nameOf(result), result.getMethod().getTimeOut());
		record(result, Status.FAILED);
	}

	@Override
	public void onFinish(ITestContext context) {
		LOG.info("---- <test> END   [{}] | passed={} failed={} skipped={} ----",
				context.getName(),
				context.getPassedTests().size(),
				context.getFailedTests().size(),
				context.getSkippedTests().size());
	}

	// ------------------------------------------------------------------------

	private void record(ITestResult result, Status status) {
		Object screenshot = result.getAttribute(SCREENSHOT_ATTRIBUTE);

		ExecutionLedger.record(new TestOutcome(
				result.getTestContext().getSuite().getName(),
				result.getTestContext().getName(),
				result.getTestClass().getRealClass().getSimpleName(),
				result.getMethod().getMethodName(),
				ExecutionLedger.describeParameters(result),
				status,
				durationOf(result),
				RetryAnalyzer.retriesUsedFor(result),
				Thread.currentThread().getName(),
				ExecutionLedger.rootCauseOf(result.getThrowable()),
				screenshot == null ? "" : screenshot.toString()));
	}

	private static long durationOf(ITestResult result) {
		return result.getEndMillis() - result.getStartMillis();
	}

	private static String nameOf(ITestResult result) {
		String parameters = ExecutionLedger.describeParameters(result);
		return result.getTestClass().getRealClass().getSimpleName()
				+ "." + result.getMethod().getMethodName()
				+ (parameters.isBlank() ? "" : " [" + parameters + "]");
	}
}
