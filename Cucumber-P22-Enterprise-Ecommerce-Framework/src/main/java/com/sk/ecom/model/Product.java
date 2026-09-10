package com.sk.ecom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sk.ecom.utils.PriceUtils;

import java.math.BigDecimal;

/**
 * A catalogue item, as read from the UI or from {@code testdata/products.json}.
 *
 * <p>Price is kept as the raw display string ({@code "$29.99"}) and converted on
 * demand, so a mismatch in currency formatting is visible in the failure message
 * rather than being lost in a silent parse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(String name, String description, String price) {

	public static Product of(String name, String price) {
		return new Product(name, "", price);
	}

	public BigDecimal priceValue() {
		return PriceUtils.parse(price);
	}
}
