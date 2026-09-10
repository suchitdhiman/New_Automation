package com.sk.ecom.reports;

/**
 * Hand-off point between the Cucumber hooks and the reporting plugin.
 *
 * <p>The {@code @After} hook still has a live WebDriver and can capture a
 * screenshot; the plugin that writes the report runs slightly later, when the
 * driver is already gone. This {@link ThreadLocal} carries the base64 image
 * across that boundary without a static free-for-all.
 */
public final class ScreenshotStore {

	private static final ThreadLocal<String> FAILURE_SCREENSHOT = new ThreadLocal<>();

	private ScreenshotStore() {
		throw new IllegalStateException("Utility class");
	}

	public static void put(String base64) {
		FAILURE_SCREENSHOT.set(base64);
	}

	/** Reads and clears in one shot so an image is never attached twice. */
	public static String take() {
		String value = FAILURE_SCREENSHOT.get();
		FAILURE_SCREENSHOT.remove();
		return value;
	}

	public static void clear() {
		FAILURE_SCREENSHOT.remove();
	}
}
