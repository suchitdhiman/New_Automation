package com.sk.ecom.utils;

/**
 * Builds locator fragments safely.
 *
 * <p>{@link #xpathLiteral(String)} exists because XPath 1.0 has no escape
 * character: a product called {@code Men's Jacket} breaks any XPath built by
 * naive string concatenation. Splitting the value across {@code concat()} is the
 * only correct fix, and doing it in one place means no page object has to think
 * about it.
 */
public final class LocatorUtils {

	private LocatorUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String xpathLiteral(String value) {
		if (!value.contains("'")) {
			return "'" + value + "'";
		}
		if (!value.contains("\"")) {
			return "\"" + value + "\"";
		}
		// Contains both quote characters: build concat('a', "'", 'b') form.
		StringBuilder sb = new StringBuilder("concat(");
		String[] parts = value.split("'", -1);
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) {
				sb.append(", \"'\", ");
			}
			sb.append("'").append(parts[i]).append("'");
		}
		return sb.append(")").toString();
	}

	/** {@code "Sauce Labs Backpack"} &rarr; {@code "sauce-labs-backpack"}. */
	public static String slug(String name) {
		return name.trim().toLowerCase().replace(' ', '-');
	}
}
