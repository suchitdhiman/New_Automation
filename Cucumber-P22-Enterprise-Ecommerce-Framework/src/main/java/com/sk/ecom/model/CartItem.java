package com.sk.ecom.model;

import com.sk.ecom.utils.PriceUtils;

import java.math.BigDecimal;

/** One line of the shopping cart. */
public record CartItem(String name, String price, int quantity) {

	public BigDecimal unitPrice() {
		return PriceUtils.parse(price);
	}

	public BigDecimal lineTotal() {
		return unitPrice().multiply(BigDecimal.valueOf(quantity));
	}
}
