package com.sk.tests.skip;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.ConfigManager;
import com.sk.core.Groups;

/**
 * CONCEPT 10 - SKIPPED, and the four different ways a test gets there.
 *
 * <table border="1">
 *   <caption>How a test ends up skipped</caption>
 *   <tr><th>Cause</th><th>Shows in the report?</th><th>Use it when</th></tr>
 *   <tr><td>{@code throw new SkipException(...)}</td><td>Yes, SKIPPED, with your message</td>
 *       <td>A precondition is not met right now - feature flag off, environment lacks the data.</td></tr>
 *   <tr><td>A {@code dependsOnMethods} / {@code dependsOnGroups} target failed</td><td>Yes, SKIPPED</td>
 *       <td>Automatic. This is the point of dependencies.</td></tr>
 *   <tr><td>A {@code @BeforeMethod} or {@code @BeforeClass} threw</td><td>Yes, SKIPPED</td>
 *       <td>Automatic. See {@link SkipBySetupTest}.</td></tr>
 *   <tr><td>{@code @Test(enabled = false)}</td><td><b>No - it vanishes entirely</b></td>
 *       <td>Almost never. Prefer a SkipException with a reason.</td></tr>
 * </table>
 *
 * <h2>The distinction that matters</h2>
 * A skip means "we did not check this". A pass means "we checked it and it was
 * fine". Turning an inconvenient failure into a skip to get a green build is
 * how a suite stops being worth running - which is why
 * {@code TestExecutionListener} logs every skip at WARN with its reason, and the
 * summary reports skips as their own column rather than folding them into
 * passes.
 */
public class SkipScenarioTest extends BaseTest {

	/**
	 * The explicit form. The message ends up in the report, so write it for
	 * whoever reads the report at 9am, not for yourself at 5pm.
	 */
	@Test(groups = { Groups.CONCEPT, Groups.REGRESSION },
			description = "SKIPPED on purpose - demonstrates SkipException with a reason")
	public void skippedBecauseTheFeatureFlagIsOff() {
		boolean loyaltyPointsEnabled = ConfigManager.getBoolean("feature.loyalty.points");

		if (!loyaltyPointsEnabled) {
			throw new SkipException("Loyalty points are not enabled in the "
					+ ConfigManager.activeEnvironment() + " environment (feature.loyalty.points=false). "
					+ "Nothing was verified.");
		}

		Assert.fail("Unreachable while the flag is off");
	}

	/**
	 * Conditional skip. On Chrome this runs and passes; on Firefox it skips with
	 * an explanation rather than failing on a browser-specific quirk.
	 */
	@Test(groups = { Groups.CONCEPT, Groups.REGRESSION },
			description = "Skips itself on browsers where the check does not apply")
	public void runsOnChromiumBrowsersOnly() {
		String browser = ConfigManager.get("browser").toLowerCase();

		if (!browser.equals("chrome") && !browser.equals("edge")) {
			throw new SkipException("This check relies on a Chromium-only devtools behaviour; "
					+ "current browser is [" + browser + "].");
		}

		log.info("Running the Chromium-only check on [{}]", browser);
		Assert.assertTrue(true, "Chromium path verified");
	}

	@Test(groups = { Groups.CONCEPT, Groups.REGRESSION },
			description = "The control case - preconditions are met, so it actually runs")
	public void runsNormallyWhenPreconditionsAreMet() {
		Assert.assertFalse(ConfigManager.baseUrl().isBlank(), "base.url should be configured");
		log.info("Preconditions met - this one is a genuine PASSED, not a skip dressed up as one.");
	}

	// -- skip caused by a failed dependency -----------------------------------

	/**
	 * Fails deliberately so the two methods below can demonstrate a
	 * dependency-driven skip. In {@link Groups#DEMO_STATUS} so it never runs in
	 * a real suite.
	 */
	@Test(groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "FAILS ON PURPOSE - the root of a dependency chain")
	public void demoRootStepFails() {
		Assert.fail("Deliberate failure. Everything that depends on this should now be SKIPPED, not FAILED.");
	}

	@Test(dependsOnMethods = "demoRootStepFails", groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "SKIPPED automatically - its dependency failed")
	public void demoDependentIsSkipped() {
		Assert.fail("Never reached: TestNG skips this because demoRootStepFails() failed.");
	}

	@Test(dependsOnMethods = "demoRootStepFails", alwaysRun = true,
			groups = { Groups.CONCEPT, Groups.DEMO_STATUS },
			description = "PASSES even though its dependency failed, because alwaysRun=true")
	public void demoAlwaysRunSurvivesAFailedDependency() {
		log.info("alwaysRun=true, so a failed dependency does not stop this from running.");
		Assert.assertTrue(true, "Reporting and cleanup hooks must survive a broken chain");
	}

	// -- the one that is NOT a skip -------------------------------------------

	/**
	 * {@code enabled = false} produces no report row at all. Six months later
	 * nobody remembers this test exists, and the coverage it used to give is
	 * gone silently. A {@code SkipException} with a ticket number is almost
	 * always the better choice.
	 */
	@Test(enabled = false, groups = Groups.CONCEPT,
			description = "Disabled, not skipped - it will not appear in any report")
	public void disabledIsNotTheSameAsSkipped() {
		Assert.fail("If this appears in a report, enabled=false stopped working.");
	}
}
