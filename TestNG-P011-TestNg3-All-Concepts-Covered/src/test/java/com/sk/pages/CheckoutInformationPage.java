package com.sk.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.sk.model.CheckoutScenario;

/**
 * Checkout step one: the shipping details form.
 *
 * <p>This is the page the Excel-driven validation tests hammer, so it exposes
 * both a "this should work" path and a "stay here and show me the error" path.
 */
public class CheckoutInformationPage extends BasePage {

	private static final By FIRST_NAME = By.id("first-name");
	private static final By LAST_NAME = By.id("last-name");
	private static final By POSTAL_CODE = By.id("postal-code");
	private static final By CONTINUE_BUTTON = By.id("continue");
	private static final By CANCEL_BUTTON = By.id("cancel");
	private static final By ERROR_BANNER = By.cssSelector("h3[data-test=\"error\"]");

	public CheckoutInformationPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return currentUrl().contains("checkout-step-one.html") && isPresent(CONTINUE_BUTTON);
	}

	public void assertLoaded() {
		assertLoaded(CONTINUE_BUTTON);
	}

	public CheckoutInformationPage enterShippingDetails(String firstName, String lastName, String zipCode) {
		log.info("Shipping details: [{}] [{}] [{}]", firstName, lastName, zipCode);
		type(FIRST_NAME, firstName);
		type(LAST_NAME, lastName);
		type(POSTAL_CODE, zipCode);
		return this;
	}

	public CheckoutInformationPage enterShippingDetails(CheckoutScenario scenario) {
		return enterShippingDetails(scenario.firstName(), scenario.lastName(), scenario.zipCode());
	}

	/** Happy path - the form is valid and we move on to the order summary. */
	public CheckoutOverviewPage continueToOverview() {
		click(CONTINUE_BUTTON);
		CheckoutOverviewPage overview = new CheckoutOverviewPage(driver);
		overview.assertLoaded();
		return overview;
	}

	/** Negative path - the form is rejected and we expect to still be here. */
	public CheckoutInformationPage continueExpectingRejection() {
		click(CONTINUE_BUTTON);
		return this;
	}

	public boolean isErrorDisplayed() {
		return isPresent(ERROR_BANNER);
	}

	public String errorMessage() {
		return textOf(ERROR_BANNER);
	}

	public CartPage cancel() {
		click(CANCEL_BUTTON);
		return new CartPage(driver);
	}
}
