package com.sk.tests.retry;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.annotations.Flaky;
import com.sk.core.BaseTest;
import com.sk.core.Groups;
import com.sk.listeners.RetryAnalyzer;

/**
 * CONCEPT 8a - retry analyzers.
 *
 * <p>Two ways to attach one, both shown here:
 * <ol>
 *   <li>{@code @Test(retryAnalyzer = RetryAnalyzer.class)} - explicit, per method.</li>
 *   <li>{@code @Flaky(maxRetries = 3)} - our own annotation, picked up by
 *       {@code RetryTransformer} (an {@code IAnnotationTransformer}) which wires
 *       the analyzer on at load time. This is how you avoid repeating the
 *       {@code retryAnalyzer} attribute on eighty methods.</li>
 * </ol>
 *
 * <p>Flakiness is simulated with a counter rather than by hitting a real site,
 * because a demonstration of retry that is itself unpredictable teaches nothing.
 * {@link SlowUserRetryTest} covers the realistic browser-side case.
 *
 * <h2>What retry does to your reporting</h2>
 * TestNG marks a discarded attempt as SKIPPED, so a naive listener reports
 * "1 passed, 2 skipped" for a test that failed twice and then passed.
 * {@code TestExecutionListener} checks {@code ITestResult.wasRetried()} and
 * drops those rows, which is why the summary at the end of a run adds up.
 *
 * <h2>The rule</h2>
 * Retry is for genuine environmental noise. It is not a fix for a race condition
 * in the application, and it is not a way to make a badly written test green. A
 * test that needs three attempts is telling you something; log it, do not
 * silence it.
 */
public class FlakyRetryTest extends BaseTest {

	private static final AtomicInteger EXPLICIT_ATTEMPTS = new AtomicInteger();
	private static final AtomicInteger ANNOTATED_ATTEMPTS = new AtomicInteger();
	private static final AtomicInteger EXHAUSTED_ATTEMPTS = new AtomicInteger();

	/**
	 * Fails the first attempt, passes the second. The default budget comes from
	 * {@code retry.max.attempts} in config.properties.
	 */
	@Test(retryAnalyzer = RetryAnalyzer.class, groups = { Groups.FLAKY, Groups.CONCEPT },
			description = "Explicit retryAnalyzer - fails once, then passes on the retry")
	public void passesOnTheSecondAttempt() {
		int attempt = EXPLICIT_ATTEMPTS.incrementAndGet();
		log.info("Attempt {} of the explicitly-retried test", attempt);

		Assert.assertTrue(attempt >= 2,
				"Simulated environment blip on attempt " + attempt + " - the retry analyzer should re-run this");
	}

	/**
	 * No {@code retryAnalyzer} attribute anywhere: {@code RetryTransformer} sees
	 * {@link Flaky} and attaches one, with the budget this annotation declares.
	 */
	@Flaky(maxRetries = 3, reason = "JIRA-4821 - third party sandbox drops the first two calls after a cold start")
	@Test(groups = { Groups.FLAKY, Groups.CONCEPT },
			description = "@Flaky(maxRetries=3) - the transformer attaches the analyzer, passes on attempt 3")
	public void annotationDrivenRetryPassesOnTheThirdAttempt() {
		int attempt = ANNOTATED_ATTEMPTS.incrementAndGet();
		log.info("Attempt {} of the @Flaky test", attempt);

		Assert.assertTrue(attempt >= 3,
				"Simulated cold-start failure on attempt " + attempt + " (budget is 3 retries)");
	}

	/**
	 * Never passes. The analyzer burns its budget, gives up, and the run gets one
	 * honest FAILED row - not one per attempt.
	 *
	 * <p>In {@link Groups#DEMO_STATUS} so only the showcase suite runs it.
	 */
	@Flaky(maxRetries = 2, reason = "Demonstration only - this one is meant to stay broken")
	@Test(groups = { Groups.FLAKY, Groups.DEMO_STATUS },
			description = "FAILS ON PURPOSE - exhausts its retry budget and reports a single failure")
	public void demoExhaustsItsRetryBudget() {
		int attempt = EXHAUSTED_ATTEMPTS.incrementAndGet();
		Assert.fail("Attempt " + attempt + " - this test never passes, by design. "
				+ "After the budget is spent you should see exactly one FAILED row in the summary.");
	}
}
