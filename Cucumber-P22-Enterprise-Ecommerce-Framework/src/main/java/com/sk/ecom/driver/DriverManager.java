package com.sk.ecom.driver;

import com.sk.ecom.exceptions.FrameworkException;

import org.openqa.selenium.WebDriver;

/**
 * Holds one WebDriver per thread.
 *
 * <p>This is the single reason the suite can run scenarios in parallel: nothing
 * anywhere else in the framework keeps a {@code WebDriver} field, everything
 * asks {@link #getDriver()} at the moment it needs one. Cucumber runs each
 * scenario on its own thread, so each scenario transparently gets its own
 * browser.
 */
public final class DriverManager {

	private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

	private DriverManager() {
		throw new IllegalStateException("Utility class");
	}

	public static void setDriver(WebDriver driver) {
		DRIVER.set(driver);
	}

	/**
	 * @throws FrameworkException when called outside a scenario, which nearly
	 *         always means a step ran before the {@code @Before} hook or after
	 *         the driver was quit.
	 */
	public static WebDriver getDriver() {
		WebDriver driver = DRIVER.get();
		if (driver == null) {
			throw new FrameworkException(
					"No WebDriver bound to thread [" + Thread.currentThread().getName() + "]. "
							+ "Either the scenario is tagged @api (no browser is started for those) "
							+ "or the driver was already quit.");
		}
		return driver;
	}

	public static boolean hasDriver() {
		return DRIVER.get() != null;
	}

	/** Quits the browser and detaches it from the thread. Safe to call twice. */
	public static void unload() {
		WebDriver driver = DRIVER.get();
		if (driver != null) {
			try {
				driver.quit();
			} finally {
				DRIVER.remove();
			}
		}
	}
}
