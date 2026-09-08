package com.sk.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Checkout step two: the order summary with item total, tax and grand total.
 *
 * <p>The three money labels come back as {@code "Item total: $29.99"}, so the
 * page object does the parsing and hands the test a {@code double}. Assertions
 * about arithmetic belong in the test; string surgery does not.
 */
public class CheckoutOverviewPage extends BasePage {

	private static final By ITEM_TOTAL = By.cssSelector(".summary_subtotal_label");
	private static final By TAX = By.cssSelector(".summary_tax_label");
	private static final By GRAND_TOTAL = By.cssSelector(".summary_total_label");
	private static final By FINISH_BUTTON = By.id("finish");
	private static final By CANCEL_BUTTON = By.id("cancel");
	private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");

	public CheckoutOverviewPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return currentUrl().contains("checkout-step-two.html") && isPresent(FINISH_BUTTON);
	}

	public void assertLoaded() {
		assertLoaded(FINISH_BUTTON);
	}

	public List<String> itemNames() {
		return textsOf(ITEM_NAME);
	}

	public double itemTotal() {
		return money(textOf(ITEM_TOTAL));
	}

	public double tax() {
		return money(textOf(TAX));
	}

	public double grandTotal() {
		return money(textOf(GRAND_TOTAL));
	}

	public CheckoutCompletePage finish() {
		click(FINISH_BUTTON);
		CheckoutCompletePage complete = new CheckoutCompletePage(driver);
		complete.assertLoaded();
		return complete;
	}

	public ProductsPage cancel() {
		click(CANCEL_BUTTON);
		return new ProductsPage(driver);
	}

	/** Turns "Item total: $29.99" into 29.99. */
	private double money(String label) {
		int dollar = label.indexOf('$');
		if (dollar < 0) {
			throw new IllegalStateException("No amount found in summary label: " + label);
		}
		return Double.parseDouble(label.substring(dollar + 1).trim());
	}
}
