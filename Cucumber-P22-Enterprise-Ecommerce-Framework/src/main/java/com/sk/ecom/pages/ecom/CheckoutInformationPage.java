package com.sk.ecom.pages.ecom;

import com.sk.ecom.model.CustomerInfo;
import com.sk.ecom.pages.base.BasePage;

import org.openqa.selenium.By;

/** Checkout step one: who the order is shipping to. */
public class CheckoutInformationPage extends BasePage {

	private static final By FIRST_NAME = By.id("first-name");
	private static final By LAST_NAME = By.id("last-name");
	private static final By POSTAL_CODE = By.id("postal-code");
	private static final By CONTINUE = By.id("continue");
	private static final By CANCEL = By.id("cancel");
	private static final By ERROR = By.cssSelector("h3[data-test='error']");

	@Override
	public boolean isAt() {
		return currentUrl().contains("checkout-step-one.html") && isDisplayed(FIRST_NAME);
	}

	public CheckoutInformationPage enterCustomer(CustomerInfo customer) {
		type(FIRST_NAME, customer.firstName(), "First name field");
		type(LAST_NAME, customer.lastName(), "Last name field");
		type(POSTAL_CODE, customer.postalCode(), "Postal code field");
		return this;
	}

	/** @return the overview page; on a validation failure the caller stays here. */
	public CheckoutOverviewPage continueToOverview() {
		click(CONTINUE, "Continue button");
		return new CheckoutOverviewPage();
	}

	/** For negative scenarios: submit without asserting we moved on. */
	public CheckoutInformationPage submitExpectingRejection() {
		click(CONTINUE, "Continue button");
		return this;
	}

	public CartPage cancel() {
		click(CANCEL, "Cancel button");
		return new CartPage();
	}

	public boolean hasError() {
		return isDisplayed(ERROR);
	}

	public String errorMessage() {
		return textOf(ERROR, "Checkout validation banner");
	}
}
