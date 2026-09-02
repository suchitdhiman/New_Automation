package testngpack.utils;

import org.apache.log4j.Logger;

/**
 * Thin wrapper over log4j so every class logs through one entry point and the
 * thread name (added by log4jConfig.properties' %t pattern) tells you which
 * parallel worker produced the line.
 */
public final class Log {

	private static final Logger LOGGER = Logger.getLogger("testngpack");

	private Log() {
	}

	public static void info(String message) {
		LOGGER.info(prefix() + message);
	}

	public static void warn(String message) {
		LOGGER.warn(prefix() + message);
	}

	public static void error(String message, Throwable t) {
		LOGGER.error(prefix() + message, t);
	}

	private static String prefix() {
		return "[" + Thread.currentThread().getName() + "] ";
	}
}
