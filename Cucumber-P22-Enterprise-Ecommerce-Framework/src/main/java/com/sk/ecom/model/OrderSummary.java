package com.sk.ecom.model;

import java.math.BigDecimal;

/**
 * The money totals shown on the checkout overview.
 *
 * <p>Exists so the arithmetic assertion lives in one testable place instead of
 * being re-derived in every checkout step.
 */
public record OrderSummary(BigDecimal itemTotal, BigDecimal tax, BigDecimal grandTotal) {

	/** @return true when {@code itemTotal + tax} equals the displayed grand total. */
	public boolean totalsAddUp() {
		return itemTotal.add(tax).compareTo(grandTotal) == 0;
	}

	@Override
	public String toString() {
		return "itemTotal=" + itemTotal + ", tax=" + tax + ", grandTotal=" + grandTotal;
	}
}
