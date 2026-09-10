package com.sk.ecom.logging;

import com.sk.ecom.reports.ExtentTestManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * The one logging entry point for the whole framework.
 *
 * <p>Two things make this more than a thin wrapper:
 *
 * <ul>
 *   <li>{@link #step(String)} writes to Log4j2 <em>and</em> to the Extent node
 *       for the current thread, so the HTML report reads like a narrative
 *       without every page object knowing that Extent exists.</li>
 *   <li>The scenario name is pushed into the Log4j2 {@link ThreadContext}, so a
 *       parallel run produces a log file you can actually untangle.</li>
 * </ul>
 */
public final class Log {

	private static final Logger LOGGER = LogManager.getLogger("Framework");
	private static final String SCENARIO_KEY = "scenario";

	private Log() {
		throw new IllegalStateException("Utility class");
	}

	/** Tags every subsequent log line on this thread with the scenario name. */
	public static void startScenario(String scenarioName) {
		ThreadContext.put(SCENARIO_KEY, scenarioName);
	}

	public static void endScenario() {
		ThreadContext.remove(SCENARIO_KEY);
	}

	public static void info(String message) {
		LOGGER.info(message);
	}

	public static void debug(String message) {
		LOGGER.debug(message);
	}

	public static void warn(String message) {
		LOGGER.warn(message);
	}

	public static void error(String message) {
		LOGGER.error(message);
	}

	public static void error(String message, Throwable throwable) {
		LOGGER.error(message, throwable);
	}

	/** Logs to the console/file appenders and to the Extent report together. */
	public static void step(String message) {
		LOGGER.info(message);
		ExtentTestManager.logInfo(message);
	}

	/** Same as {@link #step(String)} but formatted, to avoid string building at call sites. */
	public static void step(String template, Object... args) {
		step(String.format(template, args));
	}

	/** A soft problem worth surfacing in the report without failing the step. */
	public static void warnStep(String message) {
		LOGGER.warn(message);
		ExtentTestManager.logWarning(message);
	}
}
