package com.sk.ecom.reports;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

/**
 * Per-thread Extent state.
 *
 * <p>Two {@link ThreadLocal}s: one for the scenario node and one for the step
 * node currently executing. Page objects call {@link #logInfo(String)} without
 * knowing which of the two is active — the step node wins when there is one,
 * which is what produces the nested "scenario &rarr; step &rarr; action" tree in
 * the HTML report.
 *
 * <p>Every method is null-safe on purpose: framework classes are also used from
 * plain TestNG tests and from unit-level code where no Extent node exists.
 */
public final class ExtentTestManager {

	private static final ThreadLocal<ExtentTest> SCENARIO = new ThreadLocal<>();
	private static final ThreadLocal<ExtentTest> STEP = new ThreadLocal<>();

	private ExtentTestManager() {
		throw new IllegalStateException("Utility class");
	}

	/* --------------------------- lifecycle --------------------------- */

	public static void setScenario(ExtentTest test) {
		SCENARIO.set(test);
	}

	public static ExtentTest getScenario() {
		return SCENARIO.get();
	}

	public static void setStep(ExtentTest test) {
		STEP.set(test);
	}

	public static ExtentTest getStep() {
		return STEP.get();
	}

	public static void clearStep() {
		STEP.remove();
	}

	/** Must be called at the end of every scenario or threads leak nodes. */
	public static void unload() {
		STEP.remove();
		SCENARIO.remove();
	}

	/* ---------------------------- logging ---------------------------- */

	private static ExtentTest active() {
		ExtentTest step = STEP.get();
		return step != null ? step : SCENARIO.get();
	}

	public static void logInfo(String message) {
		ExtentTest test = active();
		if (test != null) {
			test.info(message);
		}
	}

	public static void logPass(String message) {
		ExtentTest test = active();
		if (test != null) {
			test.pass(message);
		}
	}

	public static void logWarning(String message) {
		ExtentTest test = active();
		if (test != null) {
			test.warning(message);
		}
	}

	public static void logFail(String message) {
		ExtentTest test = active();
		if (test != null) {
			test.fail(message);
		}
	}

	/**
	 * Embeds a screenshot as base64 so the HTML report stays a single portable
	 * file — no {@code screenshots/} folder to zip up alongside it.
	 */
	public static void attachScreenshot(String base64, String title) {
		ExtentTest test = active();
		if (test == null || base64 == null || base64.isBlank()) {
			return;
		}
		test.info(title, MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
	}
}
