package com.sk.model;

/**
 * One row of the {@code CheckoutData} sheet - the shipping form on step one of
 * checkout, plus what we expect the application to do with it.
 */
public record CheckoutScenario(
		String testCaseId,
		String firstName,
		String lastName,
		String zipCode,
		boolean shouldSucceed,
		String expectedError,
		boolean execute) {

	@Override
	public String toString() {
		return testCaseId + " [" + firstName + "/" + lastName + "/" + zipCode + "]";
	}
}
