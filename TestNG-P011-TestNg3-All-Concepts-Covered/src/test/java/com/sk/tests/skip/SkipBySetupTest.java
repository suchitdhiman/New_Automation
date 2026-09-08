package com.sk.tests.skip;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.Groups;

/**
 * CONCEPT 10 (continued) - when a configuration method skips, everything it was
 * setting up gets skipped with it.
 *
 * <p>Throwing {@link SkipException} from a {@code @BeforeMethod} skips that one
 * test. Throwing it from {@code @BeforeClass} skips the whole class. That is the
 * clean way to say "this entire area is not testable in this environment"
 * without commenting out code or littering every method with the same guard.
 *
 * <p>Contrast with a {@code @BeforeMethod} that throws something <em>else</em>:
 * that is a configuration <b>failure</b>, and the tests are still skipped but
 * the run goes red. Skip is for "cannot check this"; failure is for "the setup
 * is broken".
 *
 * <p>In {@link Groups#DEMO_STATUS} so it only runs in the showcase suite - a
 * class that always skips has no business in a real regression run.
 */
public class SkipBySetupTest extends BaseTest {

	@BeforeMethod(alwaysRun = true)
	public void skipEverythingInThisClass() {
		throw new SkipException("The bulk-order module is not deployed to this environment, "
				+ "so none of its tests can run. All methods in this class will be reported as SKIPPED.");
	}

	@Test(groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "SKIPPED - its @BeforeMethod threw a SkipException")
	public void bulkOrderCanBeCreated() {
		Assert.fail("Never reached - the @BeforeMethod skipped this test before it started.");
	}

	@Test(groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "SKIPPED - same reason, one skip per test method")
	public void bulkOrderCanBeCancelled() {
		Assert.fail("Never reached.");
	}
}
