package com.sk.core;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The browsers this framework knows how to start.
 *
 * <p>Kept as an enum rather than a raw String so a typo in a suite XML
 * ({@code <parameter name="browser" value="chorme"/>}) fails immediately with a
 * readable message, instead of surfacing as a NullPointerException three
 * classes later.
 */
public enum Browser {

	CHROME,
	FIREFOX,
	EDGE;

	public static Browser from(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Browser name was null/blank. Supported values: " + supported());
		}
		return Arrays.stream(values())
				.filter(browser -> browser.name().equalsIgnoreCase(value.trim()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"Unsupported browser [" + value + "]. Supported values: " + supported()));
	}

	private static String supported() {
		return Arrays.stream(values())
				.map(browser -> browser.name().toLowerCase())
				.collect(Collectors.joining(", "));
	}
}
