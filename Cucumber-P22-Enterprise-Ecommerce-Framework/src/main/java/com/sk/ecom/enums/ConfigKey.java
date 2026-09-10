package com.sk.ecom.enums;

/**
 * Every configuration key the framework understands, in one place.
 *
 * <p>Using an enum instead of raw strings means a typo is a compile error and
 * the IDE can list the whole surface of the configuration for you.
 */
public enum ConfigKey {

	/* --- execution --- */
	ENV("env"),
	BROWSER("browser"),
	TARGET("target"),
	HEADLESS("headless"),
	GRID_URL("grid.url"),
	BROWSER_MAXIMIZE("browser.maximize"),
	BROWSER_INCOGNITO("browser.incognito"),
	DELETE_COOKIES("browser.delete.cookies"),
	HIGHLIGHT_ELEMENTS("browser.highlight.elements"),

	/* --- application under test --- */
	APP_BASE_URL("app.base.url"),
	APP_ADVANCED_URL("app.advanced.url"),
	API_BASE_URI("api.base.uri"),

	/* --- timeouts (seconds) --- */
	TIMEOUT_EXPLICIT("timeout.explicit"),
	TIMEOUT_PAGE_LOAD("timeout.pageload"),
	TIMEOUT_SCRIPT("timeout.script"),
	TIMEOUT_IMPLICIT("timeout.implicit"),
	TIMEOUT_POLLING_MILLIS("timeout.polling.millis"),

	/* --- resilience --- */
	RETRY_COUNT("retry.count"),
	RETRY_FAILED_SCENARIOS("retry.failed.scenarios"),

	/* --- reporting --- */
	REPORT_TITLE("report.title"),
	REPORT_NAME("report.name"),
	REPORT_THEME("report.theme"),
	SCREENSHOT_ON_FAILURE("screenshot.on.failure"),
	SCREENSHOT_ON_PASS("screenshot.on.pass"),
	SCREENSHOT_EVERY_STEP("screenshot.every.step"),

	/* --- database --- */
	DB_ENABLED("db.enabled"),
	DB_URL("db.url"),
	DB_USER("db.user"),
	DB_PASSWORD("db.password"),
	DB_DRIVER("db.driver");

	private final String key;

	ConfigKey(String key) {
		this.key = key;
	}

	/** @return the literal key as it appears in the {@code .properties} files. */
	public String key() {
		return key;
	}
}
