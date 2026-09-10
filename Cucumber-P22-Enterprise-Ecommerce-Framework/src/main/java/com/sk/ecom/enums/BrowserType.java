package com.sk.ecom.enums;

import com.sk.ecom.exceptions.InvalidBrowserException;

import java.util.Arrays;
import java.util.Locale;

/** Browsers the {@code DriverFactory} knows how to build. */
public enum BrowserType {

	CHROME,
	FIREFOX,
	EDGE,
	SAFARI;

	/**
	 * Case-insensitive lookup used to translate {@code -Dbrowser=chrome} or a
	 * value from {@code config.properties} into an enum constant.
	 *
	 * @throws InvalidBrowserException when the value is not supported, listing
	 *                                 what <em>is</em> supported.
	 */
	public static BrowserType from(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidBrowserException("No browser was supplied. Expected one of " + Arrays.toString(values()));
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new InvalidBrowserException(
					"Unsupported browser [" + value + "]. Expected one of " + Arrays.toString(values()));
		}
	}
}
