package com.sk.core;

/**
 * Every TestNG group name used in this project, in one place.
 *
 * <p>Groups are plain strings in {@code @Test(groups = ...)}, which means a typo
 * silently produces a group nobody ever runs. Referencing a constant makes that
 * a compile error instead. The suite XML files still have to spell the names
 * out, so the {@code // Groups.SMOKE} style comments there are the reminder to
 * keep both sides in step.
 */
public final class Groups {

	private Groups() {
		// constants holder
	}

	/** Fast, must-never-break checks. First gate of every pipeline. */
	public static final String SMOKE = "smoke";

	/** Slightly wider than smoke, still expected to finish in a couple of minutes. */
	public static final String SANITY = "sanity";

	/** The full suite. Runs nightly. */
	public static final String REGRESSION = "regression";

	/** Full user journeys that cross several pages. */
	public static final String E2E = "e2e";

	/** Invalid input, locked accounts, validation messages. */
	public static final String NEGATIVE = "negative";

	/** Anything driven from a DataProvider or a spreadsheet. */
	public static final String DATA_DRIVEN = "data-driven";

	/** Known-unstable tests. RetryTransformer only attaches a retry analyzer here. */
	public static final String FLAKY = "flaky";

	/** Pure TestNG mechanics with no browser involved. Handy to exclude from UI runs. */
	public static final String CONCEPT = "concept";

	/**
	 * Tests that are <b>designed</b> to fail or skip, so the reporting side of
	 * the framework has something to show.
	 *
	 * <p>Excluded from every real suite and run only by
	 * {@code suites/09-result-status-showcase.xml}. A deliberate failure sitting
	 * in the nightly run is how teams learn to ignore red builds.
	 */
	public static final String DEMO_STATUS = "demo-status";

	// Functional areas of the application ------------------------------------
	public static final String LOGIN = "login";
	public static final String CATALOG = "catalog";
	public static final String CART = "cart";
	public static final String CHECKOUT = "checkout";
}
