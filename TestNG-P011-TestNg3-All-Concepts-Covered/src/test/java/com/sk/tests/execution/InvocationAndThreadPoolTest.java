package com.sk.tests.execution;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.Groups;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 5c - {@code invocationCount}, {@code threadPoolSize},
 * {@code successPercentage} and how {@code timeOut} behaves alongside them.
 *
 * <table border="1">
 *   <caption>What each attribute does</caption>
 *   <tr><th>Attribute</th><th>Meaning</th></tr>
 *   <tr><td>{@code invocationCount = 6}</td><td>Run the method six times. Six rows in the report.</td></tr>
 *   <tr><td>{@code threadPoolSize = 3}</td><td>Spread those invocations over three threads. <b>Ignored unless invocationCount &gt; 1.</b></td></tr>
 *   <tr><td>{@code successPercentage = 70}</td><td>Pass overall if at least 70% of invocations passed.</td></tr>
 *   <tr><td>{@code timeOut = 8000}</td><td>With a thread pool this is the budget for <b>all</b> invocations together, not per invocation.</td></tr>
 *   <tr><td>{@code skipFailedInvocations = true}</td><td>After the first failure, skip the remaining invocations instead of repeating it.</td></tr>
 * </table>
 *
 * <p>Where this earns its place on a real project: soak-testing a flaky
 * endpoint, proving a page object is thread safe, or reproducing a race that
 * only shows up one run in twenty.
 *
 * <p>Assertions about the thread pool live in separate methods that
 * {@code dependsOnMethods} the invocation method - that is the only reliable
 * way to run something <em>after</em> all invocations have finished.
 */
public class InvocationAndThreadPoolTest extends BaseTest {

	private static final String POOL_SCOPE = "invocation-pool";

	private static final AtomicInteger PLAIN_INVOCATIONS = new AtomicInteger();
	private static final AtomicInteger POOLED_INVOCATIONS = new AtomicInteger();
	private static final AtomicInteger PERCENTAGE_INVOCATIONS = new AtomicInteger();

	// -- invocationCount on its own ------------------------------------------

	@Test(invocationCount = 4, groups = Groups.CONCEPT,
			description = "invocationCount=4 - same method, four sequential runs, four report rows")
	public void runsFourTimesSequentially() {
		int run = PLAIN_INVOCATIONS.incrementAndGet();
		log.info("Sequential invocation {} of 4 on thread [{}]", run, Thread.currentThread().getName());

		Assert.assertTrue(run <= 4, "TestNG should not exceed the configured invocationCount");
	}

	@Test(dependsOnMethods = "runsFourTimesSequentially", groups = Groups.CONCEPT,
			description = "Verifies all four invocations actually happened")
	public void verifyFourInvocationsHappened() {
		Assert.assertEquals(PLAIN_INVOCATIONS.get(), 4,
				"invocationCount=4 should produce exactly four executions");
	}

	// -- invocationCount + threadPoolSize ------------------------------------

	/**
	 * Six invocations across a pool of three. The short sleep is not padding:
	 * without it the first thread finishes each run before the next is
	 * dispatched, and the pool never gets a chance to spread the work.
	 */
	@Test(invocationCount = 6, threadPoolSize = 3, timeOut = 15000, groups = Groups.CONCEPT,
			description = "invocationCount=6, threadPoolSize=3, timeOut=15000 covers all six runs together")
	public void runsSixTimesAcrossThreeThreads() throws InterruptedException {
		int run = POOLED_INVOCATIONS.incrementAndGet();
		ThreadAuditor.record(POOL_SCOPE, "invocation-" + run);

		log.info("Pooled invocation {} of 6 on thread [{}]", run, Thread.currentThread().getName());
		Thread.sleep(150);
	}

	@Test(dependsOnMethods = "runsSixTimesAcrossThreeThreads", groups = Groups.CONCEPT,
			description = "Proves the thread pool really was used")
	public void verifyThreadPoolWasUsed() {
		Set<String> threads = ThreadAuditor.threadsUsedIn(POOL_SCOPE);
		log.info("Thread pool distribution: {}", ThreadAuditor.report(POOL_SCOPE));

		Assert.assertEquals(POOLED_INVOCATIONS.get(), 6, "All six invocations should have run");
		Assert.assertTrue(threads.size() > 1,
				"threadPoolSize=3 should spread six invocations over more than one thread, but they all ran on "
						+ threads);
		Assert.assertTrue(threads.size() <= 3,
				"TestNG must not exceed the configured pool size. Threads seen: " + threads);
	}

	// -- successPercentage ---------------------------------------------------

	/**
	 * Ten invocations, of which two are rigged to fail - 80% success against a
	 * 70% bar, so TestNG lets it through.
	 *
	 * <p>The failing invocations do not vanish: TestNG fires
	 * {@code onTestFailedButWithinSuccessPercentage}, and
	 * {@code TestExecutionListener} logs them at WARN before recording the row
	 * as passed. Silent tolerance would be worse than no tolerance.
	 *
	 * <p>Use it for genuinely statistical checks. It is not a licence to leave a
	 * broken test in the suite.
	 */
	@Test(invocationCount = 10, successPercentage = 70, groups = Groups.CONCEPT,
			description = "successPercentage=70 - two of ten invocations fail and the method still passes")
	public void toleratesTwoFailuresInTen() {
		int run = PERCENTAGE_INVOCATIONS.incrementAndGet();

		if (run == 3 || run == 7) {
			Assert.fail("Rigged failure on invocation " + run + " - 8 of 10 still pass, which clears the 70% bar");
		}
		log.info("successPercentage invocation {} of 10 passed", run);
	}

	// -- skipFailedInvocations -----------------------------------------------

	/**
	 * {@code skipFailedInvocations = true} stops the moment one invocation
	 * fails, and reports the rest as skipped. Without it, a method that fails
	 * for a structural reason fails a hundred times and buries the report.
	 */
	@Test(invocationCount = 5, skipFailedInvocations = true, groups = Groups.CONCEPT,
			description = "skipFailedInvocations=true - all five pass here, so nothing is skipped")
	public void stopsRepeatingOnceSomethingBreaks() {
		Assert.assertTrue(true, "Kept green on purpose - flip the assertion to watch invocations 2..5 skip");
	}
}
