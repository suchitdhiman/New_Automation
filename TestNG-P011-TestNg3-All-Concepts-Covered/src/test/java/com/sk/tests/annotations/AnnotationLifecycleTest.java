package com.sk.tests.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sk.core.Groups;

/**
 * CONCEPT 1 - all ten configuration annotations, and the order TestNG runs them in.
 *
 * <p>Rather than describing the order in a comment, the class <b>records</b> it
 * as it happens and then asserts on it. If a future TestNG version changes the
 * order, this test goes red and tells you.
 *
 * <p>The order, outermost first:
 * <pre>
 *   &#64;BeforeSuite      once per &lt;suite&gt;
 *     &#64;BeforeTest      once per &lt;test&gt; tag
 *       &#64;BeforeClass    once per class
 *         &#64;BeforeGroups  before the first method of a named group
 *           &#64;BeforeMethod  before EVERY &#64;Test method
 *             &#64;Test
 *           &#64;AfterMethod
 *         &#64;AfterGroups
 *       &#64;AfterClass
 *     &#64;AfterTest
 *   &#64;AfterSuite
 * </pre>
 *
 * <p>The two people usually get wrong:
 * <ul>
 *   <li>{@code @BeforeTest} is about the {@code <test>} <b>tag</b> in the suite
 *       XML, not about a test method. Nothing to do with {@code @Test}.</li>
 *   <li>{@code @BeforeGroups} only fires if some method in that group is
 *       actually going to run in this {@code <test>} block.</li>
 * </ul>
 *
 * <p><b>Must run sequentially</b> - the recorded order is only meaningful on one
 * thread, so this class lives in a {@code parallel="false"} block in every
 * suite that includes it.
 *
 * <p>Deliberately does not extend BaseTest: an inherited {@code @BeforeClass}
 * would appear in the recording and muddy the lesson.
 */
public class AnnotationLifecycleTest {

	private static final Logger LOG = LogManager.getLogger(AnnotationLifecycleTest.class);

	/** Synchronised only as a belt-and-braces measure; the class runs on one thread. */
	private static final List<String> ORDER = Collections.synchronizedList(new ArrayList<>());

	private static final String LIFECYCLE_GROUP = "lifecycle";

	private static void record(String step) {
		ORDER.add(step);
		LOG.info("[{}] step {} -> {}", Thread.currentThread().getName(), ORDER.size(), step);
	}

	// -- the ten annotations -------------------------------------------------

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		record("@BeforeSuite");
	}

	@BeforeTest(alwaysRun = true)
	public void beforeTest() {
		record("@BeforeTest");
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		record("@BeforeClass");
	}

	@BeforeGroups(groups = LIFECYCLE_GROUP, alwaysRun = true)
	public void beforeGroups() {
		record("@BeforeGroups");
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() {
		record("@BeforeMethod");
	}

	@Test(priority = 1, groups = { LIFECYCLE_GROUP, Groups.CONCEPT },
			description = "First test method - proves @BeforeSuite/@BeforeTest/@BeforeClass/@BeforeGroups all ran before it")
	public void firstTest() {
		record("@Test firstTest");
		Assert.assertEquals(ORDER.get(0), "@BeforeSuite", "@BeforeSuite must be the very first thing to run");
		Assert.assertEquals(ORDER.get(1), "@BeforeTest", "@BeforeTest runs once per <test> tag, after @BeforeSuite");
	}

	@Test(priority = 2, groups = { LIFECYCLE_GROUP, Groups.CONCEPT },
			description = "Second test method - proves @BeforeMethod/@AfterMethod wrap every test, not just the first")
	public void secondTest() {
		record("@Test secondTest");
	}

	@Test(priority = 3, groups = { LIFECYCLE_GROUP, Groups.CONCEPT },
			description = "Asserts the full recorded execution order")
	public void verifyExecutionOrder() {
		record("@Test verifyExecutionOrder");

		List<String> expected = List.of(
				"@BeforeSuite",
				"@BeforeTest",
				"@BeforeClass",
				"@BeforeGroups",
				"@BeforeMethod",
				"@Test firstTest",
				"@AfterMethod",
				"@BeforeMethod",
				"@Test secondTest",
				"@AfterMethod",
				"@BeforeMethod",
				"@Test verifyExecutionOrder");

		List<String> actual = List.copyOf(ORDER);

		Assert.assertEquals(actual.size(), expected.size(),
				"Unexpected number of lifecycle steps. Recorded: " + actual);
		Assert.assertEquals(actual, expected,
				"TestNG did not run the configuration annotations in the documented order.");

		LOG.info("Verified lifecycle order: {}", actual);
	}

	@AfterMethod(alwaysRun = true)
	public void afterMethod() {
		record("@AfterMethod");
	}

	@AfterGroups(groups = LIFECYCLE_GROUP, alwaysRun = true)
	public void afterGroups() {
		record("@AfterGroups");
	}

	@AfterClass(alwaysRun = true)
	public void afterClass() {
		record("@AfterClass");
	}

	@AfterTest(alwaysRun = true)
	public void afterTest() {
		record("@AfterTest");
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		record("@AfterSuite");
		// Nothing can assert after this point, so print the tail of the sequence
		// that the last @Test could not see.
		LOG.info("Complete lifecycle for this class: {}", ORDER);
	}
}
