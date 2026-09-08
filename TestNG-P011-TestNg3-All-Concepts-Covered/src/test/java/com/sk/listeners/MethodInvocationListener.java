package com.sk.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.sk.utils.ScreenshotUtil;

/**
 * {@code IInvokedMethodListener} - wraps <b>every</b> invocation, configuration
 * methods included ({@code @BeforeMethod}, {@code @AfterClass} and friends).
 * {@code ITestListener} never sees those, which is exactly why this listener
 * exists.
 *
 * <p>Two jobs here:
 * <ol>
 *   <li>A trace line around each configuration method. When a suite dies during
 *       setup, this log is what tells you which {@code @BeforeX} it was.</li>
 *   <li><b>The failure screenshot.</b> TestNG guarantees
 *       {@link #afterInvocation} runs immediately after the method body, before
 *       any {@code @AfterMethod} gets a chance to quit the browser. Taking the
 *       screenshot from {@code ITestListener.onTestFailure} instead is the
 *       classic way to end up with a folder full of nothing, because the driver
 *       was already closed.</li>
 * </ol>
 * The path is parked on the {@link ITestResult} attributes so the reporters can
 * pick it up later.
 */
public class MethodInvocationListener implements IInvokedMethodListener {

	private static final Logger LOG = LogManager.getLogger(MethodInvocationListener.class);

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult result) {
		if (method.isConfigurationMethod()) {
			LOG.debug("config  >> {}.{}", simpleClassOf(result), method.getTestMethod().getMethodName());
		}
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult result) {
		if (method.isConfigurationMethod()) {
			LOG.debug("config  << {}.{} [{}]", simpleClassOf(result),
					method.getTestMethod().getMethodName(), statusText(result.getStatus()));
		}

		if (result.getStatus() != ITestResult.FAILURE) {
			return;
		}

		String label = simpleClassOf(result) + "." + method.getTestMethod().getMethodName();
		ScreenshotUtil.capture(label).ifPresent(
				path -> result.setAttribute(TestExecutionListener.SCREENSHOT_ATTRIBUTE, path.toString()));
	}

	private static String simpleClassOf(ITestResult result) {
		return result.getTestClass() == null ? "?" : result.getTestClass().getRealClass().getSimpleName();
	}

	private static String statusText(int status) {
		return switch (status) {
			case ITestResult.SUCCESS -> "PASS";
			case ITestResult.FAILURE -> "FAIL";
			case ITestResult.SKIP -> "SKIP";
			default -> "STATUS-" + status;
		};
	}
}
