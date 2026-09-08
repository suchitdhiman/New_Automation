package com.sk.tests.dependency;

import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.ConfigManager;
import com.sk.core.Groups;

/**
 * CONCEPT 5b - {@code dependsOnGroups}.
 *
 * <p>{@code dependsOnMethods} names methods, so it only reaches inside one class
 * (or its hierarchy). {@code dependsOnGroups} names a <em>group</em>, so it
 * works across classes and even across packages. That is what you want for a
 * preflight: "nothing in this suite runs until the environment checks pass",
 * without every test class having to know which class the checks live in.
 *
 * <p>Two behaviours worth having straight:
 * <ul>
 *   <li>If any method in the depended-on group fails, everything depending on
 *       that group is <b>SKIPPED</b>, not failed.</li>
 *   <li>{@code alwaysRun = true} opts a method out of that, which is how you
 *       keep teardown and reporting running when a preflight breaks.</li>
 * </ul>
 *
 * <p>Kept browser-free deliberately: a preflight that needs a browser to tell
 * you the environment is down is not much of a preflight.
 */
public class GroupDependencyTest extends BaseTest {

	private static final String PREFLIGHT = "preflight";

	private static final AtomicBoolean CONFIG_CHECK_PASSED = new AtomicBoolean(false);
	private static final AtomicBoolean URL_CHECK_PASSED = new AtomicBoolean(false);

	// -- the preflight group -------------------------------------------------

	@Test(groups = { PREFLIGHT, Groups.CONCEPT },
			description = "Preflight: the configuration actually loaded")
	public void configurationIsLoaded() {
		Assert.assertNotNull(ConfigManager.activeEnvironment(), "No active environment resolved");
		Assert.assertFalse(ConfigManager.baseUrl().isBlank(), "base.url must not be empty");
		Assert.assertTrue(ConfigManager.getInt("explicit.wait") > 0, "explicit.wait must be positive");

		CONFIG_CHECK_PASSED.set(true);
	}

	@Test(groups = { PREFLIGHT, Groups.CONCEPT },
			description = "Preflight: the target URL is an https endpoint")
	public void baseUrlIsSecure() {
		String url = ConfigManager.baseUrl();

		Assert.assertTrue(url.startsWith("https://"),
				"We do not point automation at plain http. Configured base.url was: " + url);

		URL_CHECK_PASSED.set(true);
	}

	// -- everything that needs the preflight ---------------------------------

	@Test(dependsOnGroups = PREFLIGHT, groups = Groups.CONCEPT,
			description = "Runs only because every method in the preflight group passed")
	public void runsOnlyAfterTheWholePreflightGroup() {
		Assert.assertTrue(CONFIG_CHECK_PASSED.get() && URL_CHECK_PASSED.get(),
				"dependsOnGroups should not let this start until every preflight method has passed. "
						+ "config=" + CONFIG_CHECK_PASSED.get() + " url=" + URL_CHECK_PASSED.get());
	}

	@Test(dependsOnGroups = PREFLIGHT, groups = Groups.CONCEPT,
			description = "A second dependant - the group can gate any number of methods, in any class")
	public void anotherMethodGatedByTheSameGroup() {
		Assert.assertEquals(ConfigManager.get("browser").isBlank(), false,
				"A browser should be resolvable by the time real tests run");
	}

	/**
	 * The escape hatch. If the preflight group had failed, the two methods above
	 * would be skipped and this one would still run - which is how you make sure
	 * a teardown, a report upload or an alert still happens on a broken
	 * environment.
	 */
	@Test(dependsOnGroups = PREFLIGHT, alwaysRun = true, groups = Groups.CONCEPT,
			description = "alwaysRun=true - executes even when the depended-on group fails")
	public void reportingStillRunsWhenThePreflightFails() {
		log.info("alwaysRun=true, so this executes regardless of the preflight outcome.");
		Assert.assertTrue(true, "Reporting hooks must not be skippable");
	}
}
