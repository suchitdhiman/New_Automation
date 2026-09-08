package com.sk.tests.suiteconfig;

import java.util.Map;

import org.openqa.selenium.HasCapabilities;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.ConfigManager;
import com.sk.core.Groups;

/**
 * CONCEPT 2 - suite properties, and how they are applied to classes from the XML.
 *
 * <p>Run this with {@code suites/02-suite-parameters.xml}. The same parameter
 * name, {@code environment}, is declared at three levels there, and the most
 * specific one wins:
 *
 * <pre>
 *   &lt;suite&gt;  &lt;parameter name="environment" value="from-suite-level"/&gt;
 *     &lt;test&gt;   &lt;parameter name="environment" value="from-test-level"/&gt;
 *       &lt;class&gt; &lt;parameter name="environment" value="from-class-level"/&gt;
 * </pre>
 *
 * <h2>The distinction worth remembering</h2>
 * <ul>
 *   <li>{@code @Parameters} injection walks <b>method &gt; class &gt; test &gt;
 *       suite</b>, so it sees the class-level value.</li>
 *   <li>{@code context.getCurrentXmlTest().getParameter(...)} only walks
 *       <b>test &gt; suite</b>. It cannot see a class-level parameter, because
 *       an {@code ITestContext} covers the whole {@code <test>} block and a
 *       class-level value is not defined at that scope.</li>
 * </ul>
 * Both are asserted below. That difference is behind a lot of confused
 * afternoons.
 *
 * <p>{@code @Optional} is not decoration: without it, running this class from a
 * suite that does not declare the parameter is a hard configuration error, and
 * every method in the class fails before it starts.
 */
public class SuiteParameterTest extends BaseWebTest {

	@Test(groups = { Groups.CONCEPT, Groups.SMOKE },
			description = "@Parameters injection resolves method > class > test > suite")
	@Parameters({ "environment" })
	public void classLevelParameterWinsOverTestAndSuite(@Optional("no-parameter-declared") String environment) {
		log.info("@Parameters gave us environment=[{}]", environment);

		Assert.assertEquals(environment, "from-class-level",
				"The <class> level <parameter> should win over the <test> and <suite> ones. "
						+ "If you see 'from-test-level', the <parameter> block moved out of <class>.");
	}

	@Test(groups = Groups.CONCEPT,
			description = "ITestContext only exposes <test> and <suite> parameters, never <class> ones")
	public void contextSeesTestLevelButNotClassLevel(ITestContext context) {
		String fromContext = context.getCurrentXmlTest().getParameter("environment");
		Map<String, String> allTestParameters = context.getCurrentXmlTest().getAllParameters();

		log.info("ITestContext gave us environment=[{}]", fromContext);
		log.info("All <test>+<suite> parameters visible here: {}", allTestParameters);

		Assert.assertEquals(fromContext, "from-test-level",
				"ITestContext resolves <test> then <suite>; it does not look inside <class>.");
		Assert.assertTrue(allTestParameters.containsKey("moduleOwner"),
				"A suite-level parameter should still be visible through the test context. Saw: " + allTestParameters);
	}

	@Test(groups = Groups.CONCEPT,
			description = "A parameter declared only at suite level is inherited by every test in the suite")
	@Parameters({ "moduleOwner" })
	public void suiteLevelParameterIsInherited(@Optional("unassigned") String moduleOwner) {
		Assert.assertEquals(moduleOwner, "checkout-platform-team",
				"Suite-level parameters flow down to every <test> and every class.");
	}

	/**
	 * The reason all of this matters. The {@code browser} parameter in the XML is
	 * what {@link BaseWebTest} used to start the driver, and here we ask the live
	 * session which browser it actually is.
	 */
	@Test(groups = { Groups.CONCEPT, Groups.SMOKE },
			description = "The browser parameter from the XML really did drive the driver")
	@Parameters({ "browser" })
	public void browserParameterDrivesTheDriver(@Optional("chrome") String browserFromXml) {
		String actual = ((HasCapabilities) driver()).getCapabilities().getBrowserName();
		log.info("XML asked for [{}], the live session reports [{}]", browserFromXml, actual);

		Assert.assertTrue(actual.toLowerCase().contains(expectedCapabilityName(browserFromXml)),
				"Suite asked for [" + browserFromXml + "] but the running browser reports [" + actual + "]");
	}

	@Test(groups = Groups.CONCEPT,
			description = "Config file values still apply where the XML says nothing")
	public void configFileFillsTheGaps(ITestContext context) {
		Assert.assertNull(context.getCurrentXmlTest().getParameter("explicit.wait"),
				"explicit.wait is intentionally not an XML parameter - it lives in config.properties.");

		Assert.assertTrue(ConfigManager.getInt("explicit.wait") > 0,
				"explicit.wait should come from config.properties (or the active env overlay).");
		Assert.assertTrue(driver().getCurrentUrl().startsWith(ConfigManager.baseUrl()),
				"BaseWebTest should have navigated to the configured base URL.");
	}

	/** Chrome reports "chrome", Edge reports "MicrosoftEdge", Firefox reports "firefox". */
	private String expectedCapabilityName(String requested) {
		return switch (requested.toLowerCase()) {
			case "edge" -> "edge";
			case "firefox" -> "firefox";
			default -> "chrome";
		};
	}
}
