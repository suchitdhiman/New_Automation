package com.sk.ecom.pages.ecom;

import com.sk.ecom.logging.Log;
import com.sk.ecom.model.CartItem;
import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.utils.LocatorUtils;
import com.sk.ecom.utils.PriceUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart.
 *
 * <p>{@link #items()} reads each row as a whole — name, price and quantity from
 * the same {@code .cart_item} element — instead of pulling three parallel lists
 * and zipping them by index. Index-based reads silently mis-pair rows the moment
 * one column renders differently, and that produces a green test on wrong data.
 */
public class CartPage extends BasePage {

	private static final By PAGE_TITLE = By.cssSelector("span.title");
	private static final By CART_ROW = By.cssSelector(".cart_item");
	private static final By ROW_NAME = By.cssSelector(".inventory_item_name");
	private static final By ROW_PRICE = By.cssSelector(".inventory_item_price");
	private static final By ROW_QTY = By.cssSelector(".cart_quantity");
	private static final By CONTINUE_SHOPPING = By.id("continue-shopping");
	private static final By CHECKOUT = By.id("checkout");

	@Override
	public boolean isAt() {
		return currentUrl().contains("cart.html") && isDisplayed(PAGE_TITLE);
	}

	public String heading() {
		return textOf(PAGE_TITLE, "Cart heading");
	}

	public int itemCount() {
		return countOf(CART_ROW);
	}

	public boolean isEmpty() {
		return itemCount() == 0;
	}

	/** One {@link CartItem} per row, read atomically from the row element. */
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

	public List<String> itemNames() {
		return items().stream().map(CartItem::name).toList();
	}

	public boolean contains(String productName) {
		return itemNames().stream().anyMatch(name -> name.equalsIgnoreCase(productName));
	}

	public int quantityOf(String productName) {
		return items().stream()
				.filter(item -> item.name().equalsIgnoreCase(productName))
				.map(CartItem::quantity)
				.findFirst()
				.orElse(0);
	}

	/** Sum of every line total — the number checkout should agree with. */
	public BigDecimal subtotal() {
		return PriceUtils.sum(items().stream().map(CartItem::lineTotal).toList());
	}

	public CartPage remove(String productName) {
		click(By.id("remove-" + LocatorUtils.slug(productName)), "Remove button for " + productName);
		Log.step("Removed [%s] from the cart", productName);
		return this;
	}

	public ProductsPage continueShopping() {
		click(CONTINUE_SHOPPING, "Continue shopping button");
		return new ProductsPage();
	}

	public CheckoutInformationPage checkout() {
		click(CHECKOUT, "Checkout button");
		return new CheckoutInformationPage();
	}

	public boolean isCheckoutEnabled() {
		return isEnabled(CHECKOUT);
	}
}
