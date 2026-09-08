package com.sk.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The cart page reached from the basket icon in the header. */
public class CartPage extends BasePage {

	private static final By PAGE_TITLE = By.cssSelector("span.title");
	private static final By CART_ITEM = By.cssSelector("div.cart_item");
	private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
	private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");
	private static final By CHECKOUT_BUTTON = By.id("checkout");
	private static final By CONTINUE_SHOPPING = By.id("continue-shopping");

	public CartPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return currentUrl().contains("cart.html") && isPresent(CHECKOUT_BUTTON);
	}

	public void assertLoaded() {
		assertLoaded(CHECKOUT_BUTTON);
	}

	public String headerTitle() {
		return textOf(PAGE_TITLE);
	}

	public int itemCount() {
		return countOf(CART_ITEM);
	}

	public List<String> itemNames() {
		return textsOf(ITEM_NAME);
	}

	public List<Double> itemPrices() {
		return textsOf(ITEM_PRICE).stream()
				.map(price -> price.replace("$", "").trim())
				.map(Double::parseDouble)
				.toList();
	}

	public boolean contains(String productName) {
		return itemNames().contains(productName);
	}

	public CartPage removeItem(String productName) {
		log.info("Removing [{}] from the cart", productName);
		click(By.xpath(String.format(
				"//div[@class=\"cart_item\"][.//*[contains(@class,\"inventory_item_name\")]"
						+ "[normalize-space()=\"%s\"]]//button",
				productName)));
		return this;
	}

	public CheckoutInformationPage checkout() {
		click(CHECKOUT_BUTTON);
		CheckoutInformationPage page = new CheckoutInformationPage(driver);
		page.assertLoaded();
		return page;
	}

	public ProductsPage continueShopping() {
		click(CONTINUE_SHOPPING);
		return new ProductsPage(driver);
	}
}
