package com.sk.ecom.enums;

import com.sk.ecom.exceptions.InvalidBrowserException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Where the browser actually runs: on this machine, or on a Selenium Grid /
 * cloud provider reached over the W3C protocol.
 */
public enum TargetType {

	LOCAL,
	REMOTE;

	public static TargetType from(String value) {
		if (value == null || value.isBlank()) {
			return LOCAL;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new InvalidBrowserException(
					"Unsupported execution target [" + value + "]. Expected one of " + Arrays.toString(values()));
		}
	}
}
