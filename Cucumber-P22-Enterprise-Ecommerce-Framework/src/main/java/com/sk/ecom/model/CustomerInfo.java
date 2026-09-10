package com.sk.ecom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The shipping identity captured during checkout.
 *
 * <p>Also acts as the target type for a Cucumber {@code DataTableType}, so a
 * feature file can write the customer as a table and the step definition
 * receives this object directly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerInfo(String firstName, String lastName, String postalCode) {

	public String fullName() {
		return (firstName + " " + lastName).trim();
	}

	/** @return a copy with one field blanked, for negative validation scenarios. */
	public CustomerInfo without(String field) {
		return switch (field.toLowerCase()) {
			case "firstname", "first name" -> new CustomerInfo("", lastName, postalCode);
			case "lastname", "last name" -> new CustomerInfo(firstName, "", postalCode);
			case "postalcode", "postal code", "zip" -> new CustomerInfo(firstName, lastName, "");
			default -> this;
		};
	}
}
