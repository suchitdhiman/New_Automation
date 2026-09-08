package com.sk.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * The one place every listener writes to, and the only place the reporters read
 * from.
 *
 * <p>TestNG already keeps its own {@code ITestContext} counters, but they are
 * per {@code <test>} block. When a suite has five {@code <test>} tags running in
 * parallel and you want a single "142 passed / 3 failed / 7 skipped" line at
 * the end, you need your own ledger.
 *
 * <p>{@link ConcurrentLinkedQueue} rather than an {@code ArrayList}: with
 * {@code parallel="methods"} several threads call {@link #record} at the same
 * instant, and an ArrayList quietly loses entries (or throws) under that.
 */
public final class ExecutionLedger {

	public enum Status {
		PASSED, FAILED, SKIPPED
	}

	/** One row per finished test method invocation. */
	public record TestOutcome(
			String suiteName,
			String xmlTestName,
			String className,
			String methodName,
			String parameters,
			Status status,
			long durationMs,
			int retryCount,
			String threadName,
			String failureReason,
			String screenshotPath) {

		public String displayName() {
			return className + "." + methodName + (parameters.isBlank() ? "" : " [" + parameters + "]");
		}
	}

	private static final Queue<TestOutcome> OUTCOMES = new ConcurrentLinkedQueue<>();

	private ExecutionLedger() {
		// static holder
	}

	public static void record(TestOutcome outcome) {
		OUTCOMES.add(outcome);
	}

	/** Snapshot, sorted so the report reads class by class instead of by finish time. */
	public static List<TestOutcome> all() {
		List<TestOutcome> snapshot = new ArrayList<>(OUTCOMES);
		snapshot.sort((a, b) -> {
			int byClass = a.className().compareTo(b.className());
			return byClass != 0 ? byClass : a.methodName().compareTo(b.methodName());
		});
		return snapshot;
	}

	public static long count(Status status) {
		return OUTCOMES.stream().filter(outcome -> outcome.status() == status).count();
	}

	public static int total() {
		return OUTCOMES.size();
	}

	public static long totalDurationMs() {
		return OUTCOMES.stream().mapToLong(TestOutcome::durationMs).sum();
	}

	public static void clear() {
		OUTCOMES.clear();
	}

	// -- shared helpers used by several listeners ----------------------------

	/**
	 * Stable identity for a single invocation. A DataProvider runs the same
	 * method many times, so the method name alone is not unique and the retry
	 * counter would be shared across every row.
	 */
	public static String keyOf(ITestResult result) {
		return result.getTestClass().getName()
				+ "#" + result.getMethod().getMethodName()
				+ "(" + describeParameters(result) + ")";
	}

	/**
	 * Renders the DataProvider arguments for a report cell.
	 *
	 * <p>{@code getParameters()} also returns the objects TestNG <em>injected</em>
	 * - an {@link ITestContext}, an {@link ITestResult}, a {@link Method}. Left
	 * in, a test whose only argument is an injected context shows up in the
	 * report as {@code [org.testng.TestRunner@66565121]}, so they are filtered
	 * out here.
	 */
	public static String describeParameters(ITestResult result) {
		Object[] parameters = result.getParameters();
		if (parameters == null || parameters.length == 0) {
			return "";
		}
		return Arrays.stream(parameters)
				.filter(value -> !isInjectedByTestNG(value))
				.map(value -> value == null ? "null" : String.valueOf(value))
				.collect(Collectors.joining(", "));
	}

	private static boolean isInjectedByTestNG(Object value) {
		return value instanceof ITestContext
				|| value instanceof ITestResult
				|| value instanceof XmlTest
				|| value instanceof Method;
	}

	public static String rootCauseOf(Throwable throwable) {
		if (throwable == null) {
			return "";
		}
		Throwable cursor = throwable;
		while (cursor.getCause() != null && cursor.getCause() != cursor) {
			cursor = cursor.getCause();
		}
		String message = cursor.getMessage();
		String firstLine = (message == null) ? cursor.getClass().getSimpleName() : message.split("\\R", 2)[0];
		return cursor.getClass().getSimpleName() + ": " + firstLine;
	}
}
