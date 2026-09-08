package com.sk.tests.execution;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.sk.core.BaseTest;
import com.sk.core.Groups;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 5d - the thread-count questions everybody eventually asks.
 *
 * <h2>Can thread-count be negative?</h2>
 * <b>No.</b> The XML model will happily hold the value - {@code XmlSuite} is a
 * plain bean and does not validate - but nothing useful happens with it. TestNG
 * builds a fixed thread pool from that number, and a fixed pool rejects
 * anything below 1 with an {@link IllegalArgumentException}. So a negative
 * {@code thread-count} is a configuration bug that surfaces at run time, not a
 * clever way to say "unlimited" or "auto".
 *
 * <p>Both halves of that are asserted below, so the answer is demonstrated
 * rather than asserted in prose.
 *
 * <h2>The other boundaries</h2>
 * <ul>
 *   <li>{@code thread-count} defaults to 5, {@code data-provider-thread-count}
 *       to 10, and they are <b>separate pools</b>. Raising one does not affect
 *       the other.</li>
 *   <li>{@code threadPoolSize} larger than {@code invocationCount} cannot help:
 *       there are only ever {@code invocationCount} tasks to hand out.</li>
 *   <li>{@code threadPoolSize} without {@code invocationCount} does nothing at
 *       all. One invocation, one thread.</li>
 *   <li>{@code parallel="none"} makes {@code thread-count} irrelevant, whatever
 *       it is set to.</li>
 * </ul>
 */
public class ThreadCountBoundaryTest extends BaseTest {

	private static final String OVERSIZED_POOL_SCOPE = "oversized-pool";
	private static final AtomicInteger OVERSIZED_INVOCATIONS = new AtomicInteger();

	// -- the negative thread-count question ----------------------------------

	@Test(groups = Groups.CONCEPT,
			description = "The XML model accepts a negative thread-count without complaining")
	public void xmlModelAcceptsANegativeThreadCountWithoutValidating() {
		XmlSuite suite = new XmlSuite();
		suite.setName("boundary-demo");
		suite.setParallel(XmlSuite.ParallelMode.METHODS);
		suite.setThreadCount(-4);

		Assert.assertEquals(suite.getThreadCount(), -4,
				"XmlSuite is a plain bean - it stores whatever the XML said, valid or not.");
	}

	@Test(groups = Groups.CONCEPT,
			description = "...but a thread pool cannot be built from it, which is why a negative value is a bug")
	public void aNegativeThreadCountCannotBuildAThreadPool() {
		IllegalArgumentException thrown = Assert.expectThrows(IllegalArgumentException.class,
				() -> Executors.newFixedThreadPool(-4));

		log.info("Executors.newFixedThreadPool(-4) rejected with: {}", thrown.getClass().getSimpleName());
		Assert.assertThrows(IllegalArgumentException.class, () -> Executors.newFixedThreadPool(0));

		// And the smallest value that does work.
		ExecutorService single = Executors.newFixedThreadPool(1);
		Assert.assertNotNull(single, "thread-count=1 is the minimum legal value");
		single.shutdownNow();
	}

	@Test(groups = Groups.CONCEPT,
			description = "The documented defaults for the two independent thread pools")
	public void defaultThreadCountsAreFiveAndTen() {
		XmlSuite suite = new XmlSuite();

		int defaultThreadCount = suite.getThreadCount();
		int defaultDataProviderThreadCount = suite.getDataProviderThreadCount();

		Assert.assertEquals(defaultThreadCount, 5,
				"thread-count defaults to 5 when the suite does not say otherwise");
		Assert.assertEquals(defaultDataProviderThreadCount, 10,
				"data-provider-thread-count defaults to 10 and is a separate pool from thread-count");
	}

	@Test(groups = Groups.CONCEPT,
			description = "A <test> inherits the suite thread-count until it overrides it")
	public void testLevelThreadCountOverridesTheSuite() {
		XmlSuite suite = new XmlSuite();
		suite.setThreadCount(8);

		XmlTest test = new XmlTest(suite);
		test.setName("inherits");
		Assert.assertEquals(test.getThreadCount(), 8, "A <test> with no thread-count inherits the suite value");

		test.setThreadCount(2);
		Assert.assertEquals(test.getThreadCount(), 2, "An explicit <test> thread-count wins");
		Assert.assertEquals(suite.getThreadCount(), 8, "...and does not leak back up to the suite");
	}

	// -- threadPoolSize larger than invocationCount --------------------------

	/**
	 * Eight threads asked for, two invocations available. TestNG cannot invent
	 * work, so at most two threads are ever used.
	 */
	@Test(invocationCount = 2, threadPoolSize = 8, groups = Groups.CONCEPT,
			description = "threadPoolSize=8 with invocationCount=2 - the pool cannot be busier than the work allows")
	public void oversizedPoolIsCappedByInvocationCount() throws InterruptedException {
		int run = OVERSIZED_INVOCATIONS.incrementAndGet();
		ThreadAuditor.record(OVERSIZED_POOL_SCOPE, "run-" + run);
		Thread.sleep(120);
	}

	@Test(dependsOnMethods = "oversizedPoolIsCappedByInvocationCount", groups = Groups.CONCEPT,
			description = "Confirms at most invocationCount threads were used")
	public void verifyOversizedPoolWasCapped() {
		Set<String> threads = ThreadAuditor.threadsUsedIn(OVERSIZED_POOL_SCOPE);
		log.info("Oversized pool used {} thread(s): {}", threads.size(), threads);

		Assert.assertEquals(OVERSIZED_INVOCATIONS.get(), 2, "Only two invocations were configured");
		Assert.assertTrue(threads.size() <= 2,
				"Cannot use more threads than there are invocations. Saw: " + threads);
	}

	// -- timeOut -------------------------------------------------------------

	@Test(timeOut = 2000, groups = Groups.CONCEPT,
			description = "timeOut on a single method - comfortably inside its budget")
	public void completesInsideItsTimeout() throws InterruptedException {
		Thread.sleep(200);
	}

	/**
	 * Deliberately blows a 500ms budget so the reports have a genuine
	 * {@code ThreadTimeoutException} in them, and
	 * {@code TestExecutionListener.onTestFailedWithTimeout} has something to
	 * report.
	 *
	 * <p>In {@link Groups#DEMO_STATUS}, so it only runs in the status showcase
	 * suite and never in a real one.
	 */
	@Test(timeOut = 500, groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "FAILS ON PURPOSE - exceeds its 500ms timeOut")
	public void demoExceedsItsTimeoutOnPurpose() throws InterruptedException {
		log.warn("This method is designed to blow its timeOut - the resulting FAILED row is the point.");
		Thread.sleep(3000);
	}
}
