package com.sk.ecom.pages.ecom;

import com.sk.ecom.pages.base.BasePage;

import org.openqa.selenium.By;

/** Order confirmation — the end of the happy path. */
public class CheckoutCompletePage extends BasePage {

	private static final By HEADER = By.cssSelector("h2.complete-header");
	private static final By BODY_TEXT = By.cssSelector(".complete-text");
	private static final By PONY_IMAGE = By.cssSelector("img.pony_express");
	private static final By BACK_HOME = By.id("back-to-products");

	@Override
	public boolean isAt() {
		return currentUrl().contains("checkout-complete.html") && isDisplayed(HEADER);
	}

	public String confirmationHeader() {
		return textOf(HEADER, "Order confirmation header");
	}

	public String confirmationMessage() {
		return textOf(BODY_TEXT, "Order confirmation message");
	}

	public boolean hasConfirmationImage() {
		return isDisplayed(PONY_IMAGE);
	}

	public ProductsPage backToCatalogue() {
		click(BACK_HOME, "Back Home button");
		return new ProductsPage();
	}
}
