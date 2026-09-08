package com.sk.listeners;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;

import com.sk.core.ConfigManager;

/**
 * {@code ISuiteListener} - fires once per {@code <suite>}, around everything
 * else.
 *
 * <p>This is where suite-wide setup belongs, and it is a better home for it
 * than {@code @BeforeSuite} on a base class: it cannot be accidentally
 * inherited, disabled by a group filter, or skipped because the class it lives
 * on was excluded from the run.
 *
 * <p>The banner it prints is genuinely useful in CI. Six months from now,
 * looking at a failed nightly, "which parallel mode and how many threads was
 * this actually running with?" is the first question, and the answer is right
 * at the top of the log.
 */
public class SuiteExecutionListener implements ISuiteListener {

	private static final Logger LOG = LogManager.getLogger(SuiteExecutionListener.class);

	@Override
	public void onStart(ISuite suite) {
		XmlSuite xml = suite.getXmlSuite();

		LOG.info("================================================================");
		LOG.info(" SUITE START      : {}", suite.getName());
		LOG.info(" Environment      : {} ({})", ConfigManager.activeEnvironment(), ConfigManager.baseUrl());
		LOG.info(" Browser default  : {} (headless={})", ConfigManager.get("browser"), ConfigManager.get("headless"));
		LOG.info(" Parallel mode    : {}", xml.getParallel());
		LOG.info(" Thread count     : {}", xml.getThreadCount());
		LOG.info(" DataProvider thr. : {}", xml.getDataProviderThreadCount());
		LOG.info(" Suite parameters : {}", describe(xml.getParameters()));
		LOG.info("================================================================");
	}

	@Override
	public void onFinish(ISuite suite) {
		LOG.info("================================================================");
		LOG.info(" SUITE END        : {}", suite.getName());
		LOG.info(" Recorded so far  : {} passed / {} failed / {} skipped (of {})",
				ExecutionLedger.count(ExecutionLedger.Status.PASSED),
				ExecutionLedger.count(ExecutionLedger.Status.FAILED),
				ExecutionLedger.count(ExecutionLedger.Status.SKIPPED),
				ExecutionLedger.total());
		LOG.info("================================================================");
	}

	private static String describe(Map<String, String> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return "(none)";
		}
		StringBuilder text = new StringBuilder();
		parameters.forEach((key, value) -> text.append(key).append("=").append(value).append("  "));
		return text.toString().trim();
	}
}
