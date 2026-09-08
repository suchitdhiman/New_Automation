package com.sk.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.sk.core.ConfigManager;

/**
 * Re-runs a failed test up to a bounded number of times.
 *
 * <h2>Two things people get wrong with retry analyzers</h2>
 * <ol>
 *   <li><b>Where the counter lives.</b> TestNG creates a fresh analyzer instance
 *       per test method, so an instance field looks like it works right up until
 *       a DataProvider feeds the same method twenty rows and they all share -
 *       or reset - the same counter. The counter here is static and keyed by
 *       class + method + parameters, so every row retries independently.</li>
 *   <li><b>Retry-everything.</b> This class is only attached where it is asked
 *       for: explicitly via {@code @Test(retryAnalyzer = RetryAnalyzer.class)},
 *       or by {@code RetryTransformer} on methods marked {@code @Flaky}. A
 *       blanket policy turns a real regression into a slow green build.</li>
 * </ol>
 *
 * <p>Attempts that are going to be retried surface as SKIPPED in TestNG. That is
 * why {@code TestExecutionListener} checks {@link ITestResult#wasRetried()}
 * before recording anything.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

	private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);

	/** invocation key -> retries already consumed. */
	private static final Map<String, AtomicInteger> ATTEMPTS = new ConcurrentHashMap<>();

	/** declaringClass#method -> budget, published by RetryTransformer from {@code @Flaky}. */
	private static final Map<String, Integer> BUDGETS = new ConcurrentHashMap<>();

	/** TestNG instantiates this reflectively, so the no-arg constructor has to stay. */
	public RetryAnalyzer() {
		// required by TestNG
	}

	@Override
	public boolean retry(ITestResult result) {
		if (result.isSuccess()) {
			return false;
		}

		int budget = budgetFor(result);
		String key = ExecutionLedger.keyOf(result);
		AtomicInteger used = ATTEMPTS.computeIfAbsent(key, ignored -> new AtomicInteger());

		if (used.get() >= budget) {
			LOG.error("GIVING UP on {} after {} attempt(s). Last failure: {}",
					key, budget + 1, ExecutionLedger.rootCauseOf(result.getThrowable()));
			return false;
		}

		int attempt = used.incrementAndGet();
		LOG.warn("RETRY {}/{} for {} | reason: {}",
				attempt, budget, key, ExecutionLedger.rootCauseOf(result.getThrowable()));
		return true;
	}

	/** How many retries this invocation has already burned - used by the reports. */
	public static int retriesUsedFor(ITestResult result) {
		AtomicInteger counter = ATTEMPTS.get(ExecutionLedger.keyOf(result));
		return counter == null ? 0 : counter.get();
	}

	/** Called by RetryTransformer so a {@code @Flaky(maxRetries = 3)} is honoured. */
	public static void registerBudget(String methodKey, int maxRetries) {
		BUDGETS.put(methodKey, maxRetries);
	}

	public static String budgetKey(String className, String methodName) {
		return className + "#" + methodName;
	}

	private int budgetFor(ITestResult result) {
		String key = budgetKey(result.getTestClass().getRealClass().getName(), result.getMethod().getMethodName());
		return BUDGETS.getOrDefault(key, ConfigManager.getInt("retry.max.attempts"));
	}

	/** Only needed if a single JVM runs several suites back to back. */
	public static void reset() {
		ATTEMPTS.clear();
	}
}
