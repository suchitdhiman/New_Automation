package com.sk.ecom.enums;

/**
 * Typed keys for the per-scenario {@code ScenarioContext} bag.
 *
 * <p>Steps in different classes hand data to each other through this enum
 * rather than through static fields, which is what keeps parallel execution
 * safe.
 */
public enum ContextKey {

	/** The user currently signed in. */
	LOGGED_IN_USER,
	/** Products the test intentionally added to the cart. */
	EXPECTED_CART_ITEMS,
	/** Product prices captured on the catalogue page, for later maths. */
	CAPTURED_PRICES,
	/** Item total read off the checkout overview page. */
	ITEM_TOTAL,
	/** Tax read off the checkout overview page. */
	TAX_AMOUNT,
	/** Grand total read off the checkout overview page. */
	GRAND_TOTAL,
	/** Customer details submitted during checkout. */
	CHECKOUT_CUSTOMER,
	/** Window handle captured before opening a new tab. */
	PARENT_WINDOW,
	/** Last REST-Assured response, shared between API steps. */
	API_RESPONSE,
	/** Request payload built by an earlier API step. */
	API_REQUEST_BODY,
	/** Rows returned by the last database query. */
	DB_RESULT,
	/** Free-form scratch value for one-off assertions. */
	SCRATCH
}
