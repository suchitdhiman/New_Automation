package com.sk.ecom.pages.ecom;

import com.sk.ecom.model.CartItem;
import com.sk.ecom.model.OrderSummary;
import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.utils.PriceUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkout step two: the money screen.
 *
 * <p>This is where an e-commerce suite earns its keep. {@link #summary()}
 * returns the three displayed totals as {@link BigDecimal} so a step can assert
 * that the application's own arithmetic is internally consistent — subtotal plus
 * tax equals total — and that the subtotal matches the cart it came from.
 */
public class CheckoutOverviewPage extends BasePage {

	private static final By CART_ROW = By.cssSelector(".cart_item");
	private static final By ROW_NAME = By.cssSelector(".inventory_item_name");
	private static final By ROW_PRICE = By.cssSelector(".inventory_item_price");
	private static final By ROW_QTY = By.cssSelector(".cart_quantity");
	private static final By SUBTOTAL = By.cssSelector(".summary_subtotal_label");
	private static final By TAX = By.cssSelector(".summary_tax_label");
	private static final By TOTAL = By.cssSelector(".summary_total_label");
	private static final By PAYMENT_INFO = By.cssSelector(".summary_value_label");
	private static final By FINISH = By.id("finish");
	private static final By CANCEL = By.id("cancel");

	@Override
	public boolean isAt() {
		return currentUrl().contains("checkout-step-two.html") && isDisplayed(TOTAL);
	}

	public List<CartItem> items() {
		List<CartItem> items = new ArrayList<>();
		for (WebElement row : findAll(CART_ROW)) {
			items.add(new CartItem(
					row.findElement(ROW_NAME).getText().trim(),
					row.findElement(ROW_PRICE).getText().trim(),
					Integer.parseInt(row.findElement(ROW_QTY).getText().trim())));
		}
		return items;
	}

	public BigDecimal itemTotal() {
		return PriceUtils.parse(textOf(SUBTOTAL, "Item total"));
	}

	public BigDecimal tax() {
		return PriceUtils.parse(textOf(TAX, "Tax"));
	}

	public BigDecimal grandTotal() {
		return PriceUtils.parse(textOf(TOTAL, "Grand total"));
	}

	public OrderSummary summary() {
		return new OrderSummary(itemTotal(), tax(), grandTotal());
	}

	/** Sum of the line items actually shown on this screen. */
	public BigDecimal calculatedItemTotal() {
		return PriceUtils.sum(items().stream().map(CartItem::lineTotal).toList());
	}

	public List<String> paymentAndShippingDetails() {
		return textsOf(PAYMENT_INFO);
	}

	public CheckoutCompletePage finish() {
		click(FINISH, "Finish button");
		return new CheckoutCompletePage();
	}

	public ProductsPage cancel() {
		click(CANCEL, "Cancel button");
		return new ProductsPage();
	}
}
