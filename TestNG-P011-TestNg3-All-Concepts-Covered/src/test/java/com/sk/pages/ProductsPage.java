package com.sk.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.sk.model.SortOption;

/**
 * The product catalogue, which is the landing page after a successful sign-in.
 *
 * <p>The add/remove buttons carry ids like
 * {@code add-to-cart-test.allthethings()-t-shirt-(red)}. Deriving that id from
 * the product name in code is a slugging exercise that breaks the first time
 * marketing renames a product, so the buttons are located <em>relative to the
 * card that contains the product name</em> instead. Slower to read, far harder
 * to break.
 */
public class ProductsPage extends BasePage {

	private static final By PAGE_TITLE = By.cssSelector("span.title");
	private static final By PRODUCT_CARD = By.cssSelector("div.inventory_item");
	private static final By PRODUCT_NAME = By.cssSelector("div.inventory_item_name, .inventory_item_name");
	private static final By PRODUCT_PRICE = By.cssSelector("div.inventory_item_price, .inventory_item_price");
	private static final By SORT_DROPDOWN = By.cssSelector("[data-test=\"product-sort-container\"]");
	private static final By CART_LINK = By.cssSelector("a.shopping_cart_link");
	private static final By CART_BADGE = By.cssSelector("span.shopping_cart_badge");
	private static final By BURGER_MENU = By.id("react-burger-menu-btn");
	private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
	private static final By LOGIN_ERROR = By.cssSelector("h3[data-test=\"error\"]");

	public ProductsPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return currentUrl().contains("inventory.html") && isPresent(PAGE_TITLE);
	}

	/**
	 * Called straight after a login attempt. If the application rejected the
	 * credentials we are still on the login screen, and the useful thing to
	 * report is the banner it showed us - not "element not found".
	 */
	public void assertLoadedOrExplain() {
		if (isLoaded()) {
			return;
		}
		String detail = isPresent(LOGIN_ERROR) ? textOf(LOGIN_ERROR) : "no error banner was shown";
		throw new IllegalStateException("Expected the products page after login, but the application said: " + detail);
	}

	public String headerTitle() {
		return textOf(PAGE_TITLE);
	}

	public int productCount() {
		return countOf(PRODUCT_CARD);
	}

	public List<String> productNames() {
		return textsOf(PRODUCT_NAME);
	}

	/** Prices as numbers, so a test can assert the sort order arithmetically. */
	public List<Double> productPrices() {
		return textsOf(PRODUCT_PRICE).stream()
				.map(price -> price.replace("$", "").trim())
				.map(Double::parseDouble)
				.toList();
	}

	public ProductsPage sortBy(SortOption option) {
		log.info("Sorting catalogue by [{}]", option.label());
		selectByValue(SORT_DROPDOWN, option.value());
		return this;
	}

	public String selectedSortLabel() {
		return selectedLabel(SORT_DROPDOWN);
	}

	public ProductsPage addToCart(String productName) {
		log.info("Adding [{}] to the cart", productName);
		click(actionButtonFor(productName));
		return this;
	}

	public ProductsPage removeFromCart(String productName) {
		log.info("Removing [{}] from the cart", productName);
		click(actionButtonFor(productName));
		return this;
	}

	/** @return the number on the cart badge, or 0 when the badge is not rendered. */
	public int cartBadgeCount() {
		if (!isPresent(CART_BADGE)) {
			return 0;
		}
		return Integer.parseInt(textOf(CART_BADGE));
	}

	public CartPage openCart() {
		click(CART_LINK);
		CartPage cart = new CartPage(driver);
		cart.assertLoaded();
		return cart;
	}

	public LoginPage logout() {
		click(BURGER_MENU);
		click(LOGOUT_LINK);
		return new LoginPage(driver);
	}

	/**
	 * The card button is a toggle: it reads "Add to cart" or "Remove" depending
	 * on state, but it is the same element either way.
	 */
	private By actionButtonFor(String productName) {
		return By.xpath(String.format(
				"//div[@class=\"inventory_item\"][.//*[contains(@class,\"inventory_item_name\")]"
						+ "[normalize-space()=\"%s\"]]//button",
				productName));
	}
}
