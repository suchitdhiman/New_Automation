package com.sk.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The order confirmation screen - the last page of the purchase journey. */
public class CheckoutCompletePage extends BasePage {

	public static final String EXPECTED_HEADER = "Thank you for your order!";

	private static final By HEADER = By.cssSelector("h2.complete-header, .complete-header");
	private static final By BODY_TEXT = By.cssSelector(".complete-text");
	private static final By PONY_IMAGE = By.cssSelector("img.pony_express");
	private static final By BACK_HOME = By.id("back-to-products");

	public CheckoutCompletePage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return currentUrl().contains("checkout-complete.html") && isPresent(HEADER);
	}

	public void assertLoaded() {
		assertLoaded(HEADER);
	}

	public String confirmationHeader() {
		return textOf(HEADER);
	}

	public String confirmationBody() {
		return textOf(BODY_TEXT);
	}

	public boolean isConfirmationImageShown() {
		return isPresent(PONY_IMAGE);
	}

	public ProductsPage backToProducts() {
		click(BACK_HOME);
		return new ProductsPage(driver);
	}
}
