package com.sk.ecom.enums;

/**
 * The condition a locator must satisfy before the framework touches it.
 *
 * <p>Every interaction in {@code BasePage} takes one of these, which is how the
 * framework stays free of {@code Thread.sleep} and of a global implicit wait.
 */
public enum WaitStrategy {

	/** Element is present in the DOM and clickable. Default for clicks. */
	CLICKABLE,
	/** Element is in the DOM and has a non-zero size. Default for reads/typing. */
	VISIBLE,
	/** Element is in the DOM; it may still be hidden. */
	PRESENCE,
	/** All elements matching the locator are visible. */
	ALL_VISIBLE,
	/** Element is gone or hidden — used for spinners and toasts. */
	INVISIBLE,
	/** No wait at all; the caller has already synchronised. */
	NONE
}
