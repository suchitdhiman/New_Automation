package com.sk.tests.annotations;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.Groups;

/**
 * CONCEPT 1 (continued) - the attributes of {@code @Test} itself.
 *
 * <p>Every attribute demonstrated here is one you will actually reach for:
 *
 * <table border="1">
 *   <caption>Attributes covered</caption>
 *   <tr><th>Attribute</th><th>What it buys you</th></tr>
 *   <tr><td>{@code description}</td><td>Readable report rows. Always fill it in.</td></tr>
 *   <tr><td>{@code priority}</td><td>Relative order, low number first. Same-priority order is undefined.</td></tr>
 *   <tr><td>{@code enabled}</td><td>Switch a test off without deleting it. It vanishes from the report entirely - it is NOT a skip.</td></tr>
 *   <tr><td>{@code expectedExceptions}</td><td>The test passes only if that exception is thrown.</td></tr>
 *   <tr><td>{@code expectedExceptionsMessageRegExp}</td><td>Narrows the above so any old exception of that type will not do.</td></tr>
 *   <tr><td>{@code timeOut}</td><td>Fails the method if it runs longer than N ms.</td></tr>
 *   <tr><td>{@code groups}</td><td>What the suite XML filters on.</td></tr>
 * </table>
 *
 * <p>Runs sequentially, like every class in the concept package.
 */
public class TestAttributesTest extends BaseTest {

	private static final List<Integer> PRIORITY_TRACE = new ArrayList<>();

	// -- priority ------------------------------------------------------------

	@Test(priority = 1, groups = Groups.CONCEPT,
			description = "priority=1 - runs first among the priority demos")
	public void priorityOne() {
		PRIORITY_TRACE.add(1);
		log.info("priority=1 executed");
	}

	@Test(priority = 5, groups = Groups.CONCEPT,
			description = "priority=5 - the numbers are relative, they do not have to be consecutive")
	public void priorityFive() {
		PRIORITY_TRACE.add(5);
		log.info("priority=5 executed");
	}

	@Test(priority = 10, groups = Groups.CONCEPT,
			description = "priority=10 - runs last and asserts the recorded order")
	public void priorityTenVerifiesOrder() {
		PRIORITY_TRACE.add(10);
		Assert.assertEquals(PRIORITY_TRACE, List.of(1, 5, 10),
				"Lower priority numbers must run first. Recorded order: " + PRIORITY_TRACE);
	}

	/**
	 * A method with no priority defaults to 0, so it runs <em>before</em> all of
	 * the above. That surprises people who assume "no priority means last".
	 */
	@Test(groups = Groups.CONCEPT, description = "No priority = priority 0, so this runs before priority=1")
	public void noPriorityMeansZero() {
		Assert.assertTrue(PRIORITY_TRACE.isEmpty(),
				"A method without an explicit priority defaults to 0 and should run before priority=1, "
						+ "but these had already run: " + PRIORITY_TRACE);
	}

	// -- enabled -------------------------------------------------------------

	/**
	 * {@code enabled = false} is the one that trips people up: this method is
	 * not reported as skipped, it is not reported <b>at all</b>. If you want it
	 * visible in the report, throw a {@code SkipException} instead - see
	 * {@code SkipScenarioTest}.
	 */
	@Test(enabled = false, groups = Groups.CONCEPT,
			description = "Disabled on purpose - will not appear anywhere in the report")
	public void disabledTest() {
		Assert.fail("If you can see this in the report, enabled=false stopped working.");
	}

	// -- expected exceptions -------------------------------------------------

	@Test(groups = Groups.CONCEPT, expectedExceptions = IllegalArgumentException.class,
			description = "Passes only because the expected exception is thrown")
	public void expectsAnException() {
		throw new IllegalArgumentException("Unsupported browser [chorme]");
	}

	/**
	 * Without the regexp, <em>any</em> IllegalArgumentException passes - including
	 * one thrown by a completely unrelated bug three lines earlier. Pin the
	 * message whenever the exception type alone is not specific.
	 */
	@Test(groups = Groups.CONCEPT,
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = ".*Unsupported browser.*",
			description = "Expected exception narrowed by a message regexp")
	public void expectsAnExceptionWithMessage() {
		throw new IllegalArgumentException("Unsupported browser [chorme]. Supported values: chrome, firefox, edge");
	}

	/**
	 * The modern alternative. {@code assertThrows} keeps the expectation next to
	 * the line that should throw, so a test with several statements cannot pass
	 * because the wrong one threw.
	 */
	@Test(groups = Groups.CONCEPT, description = "Assert.assertThrows scopes the expectation to one statement")
	public void assertThrowsIsMorePrecise() {
		IllegalArgumentException thrown = Assert.expectThrows(IllegalArgumentException.class,
				() -> com.sk.core.Browser.from("chorme"));

		Assert.assertTrue(thrown.getMessage().contains("Unsupported browser"),
				"Message should name the problem, was: " + thrown.getMessage());
	}

	// -- timeOut -------------------------------------------------------------

	/**
	 * {@code timeOut} is a per-method budget in milliseconds. TestNG runs the
	 * method on a separate thread and fails it with a
	 * {@code ThreadTimeoutException} if the budget is blown.
	 *
	 * <p>Put one on anything that talks to a network. A hung request with no
	 * timeout does not fail the build, it hangs the pipeline.
	 */
	@Test(groups = Groups.CONCEPT, timeOut = 3000,
			description = "Completes well inside its 3s budget")
	public void finishesInsideTimeout() throws InterruptedException {
		Thread.sleep(250);
		log.info("Finished comfortably inside the 3000ms budget");
	}
}
