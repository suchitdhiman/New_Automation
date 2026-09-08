package com.sk.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Foundation for <b>every</b> test class, browser or not.
 *
 * <p>Deliberately owns nothing that needs a WebDriver. Half of the classes in
 * this project demonstrate pure TestNG mechanics (invocation counts, dependency
 * graphs, assertion styles); starting a Chrome instance for those would add
 * thirty seconds to the run and prove nothing. Those extend this class;
 * anything that touches the application extends {@link BaseWebTest}.
 *
 * <p>Note there is no {@code @BeforeSuite} here. Suite-wide setup lives in
 * {@code SuiteExecutionListener} instead, because a {@code @BeforeSuite}
 * inherited by twenty classes is easy to misread as "runs twenty times".
 */
public abstract class BaseTest {

	/** One logger per concrete subclass, so log lines carry the real class name. */
	protected final Logger log = LogManager.getLogger(getClass());

	@BeforeClass(alwaysRun = true)
	public void logClassStart(ITestContext context) {
		log.info("==== START class {} | <test> = {} | thread = {} ====",
				getClass().getSimpleName(), context.getName(), Thread.currentThread().getName());
	}

	@AfterClass(alwaysRun = true)
	public void logClassEnd() {
		log.info("==== END   class {} ====", getClass().getSimpleName());
	}

	/**
	 * Reads a {@code <parameter>} declared in the suite XML at suite, test or
	 * class level, falling back to {@code config.properties} and then to the
	 * supplied default.
	 *
	 * <p>{@code context.getCurrentXmlTest().getParameter(...)} already walks the
	 * class then test then suite chain for you, which is exactly the override
	 * order the XML implies.
	 */
	protected String xmlParam(ITestContext context, String key, String fallback) {
		String fromXml = context.getCurrentXmlTest().getParameter(key);
		if (fromXml != null && !fromXml.isBlank()) {
			return fromXml.trim();
		}
		return ConfigManager.get(key, fallback);
	}
}
