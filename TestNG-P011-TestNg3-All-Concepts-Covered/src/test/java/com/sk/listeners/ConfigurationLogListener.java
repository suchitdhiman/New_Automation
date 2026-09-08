package com.sk.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

/**
 * {@code IConfigurationListener} - the only listener that reports on
 * {@code @Before*} / {@code @After*} methods as first-class results.
 *
 * <p>Worth registering on any real project. When a {@code @BeforeMethod} throws,
 * TestNG marks every test in the class as SKIPPED and the HTML report shows a
 * wall of skips with no obvious cause. This listener puts one clear ERROR line
 * in the log naming the configuration method that actually broke.
 */
public class ConfigurationLogListener implements IConfigurationListener {

	private static final Logger LOG = LogManager.getLogger(ConfigurationLogListener.class);

	@Override
	public void onConfigurationSuccess(ITestResult result) {
		LOG.debug("config OK   : {}", name(result));
	}

	@Override
	public void onConfigurationFailure(ITestResult result) {
		LOG.error("CONFIG FAILED : {} | {} -- every test that depends on it will be SKIPPED",
				name(result), ExecutionLedger.rootCauseOf(result.getThrowable()));
	}

	@Override
	public void onConfigurationSkip(ITestResult result) {
		LOG.warn("config SKIP : {}", name(result));
	}

	private static String name(ITestResult result) {
		return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
	}
}
