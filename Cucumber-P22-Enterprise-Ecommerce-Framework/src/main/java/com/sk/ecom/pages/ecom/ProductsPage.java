package com.sk.ecom.pages.ecom;

import com.sk.ecom.logging.Log;
import com.sk.ecom.model.Product;
import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.utils.LocatorUtils;
import com.sk.ecom.utils.PriceUtils;

import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product catalogue: the screen where most of an e-commerce suite lives.
 *
 * <p>Per-product locators are built from the product name at call time rather
 * than being enumerated as constants. That is what lets a single
 * {@code Scenario Outline} drive every item in the catalogue without the page
 * object growing a field per SKU.
 */
public class ProductsPage extends BasePage {

	private static final By PAGE_TITLE = By.cssSelector("span.title");
	private static final By ITEM_CARD = By.cssSelector(".inventory_item");
	private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
	private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");
	private static final By ITEM_DESCRIPTION = By.cssSelector(".inventory_item_desc");
	private static final By SORT_DROPDOWN = By.cssSelector("select.product_sort_container");

	@Override
	public boolean isAt() {
		return currentUrl().contains("inventory.html") && isDisplayed(ITEM_CARD);
	}

	public String heading() {
		return textOf(PAGE_TITLE, "Catalogue heading");
	}

	/* ------------------------------------------------------------------ */
	/* Reading the catalogue                                               */
	/* ------------------------------------------------------------------ */

	public int productCount() {
		return countOf(ITEM_CARD);
	}

	public List<String> productNames() {
		return textsOf(ITEM_NAME);
	}

	public List<String> productPrices() {
		return textsOf(ITEM_PRICE);
	}

	public List<BigDecimal> productPriceValues() {
		return PriceUtils.parseAll(productPrices());
	}

	public List<Product> allProducts() {
		List<String> names = productNames();
		List<String> prices = productPrices();
		List<String> descriptions = textsOf(ITEM_DESCRIPTION);
		return java.util.stream.IntStream.range(0, names.size())
				.mapToObj(i -> new Product(names.get(i),
						i < descriptions.size() ? descriptions.get(i) : "",
						i < prices.size() ? prices.get(i) : ""))
				.toList();
	}

	public String priceOf(String productName) {
		return textOf(priceWithinCard(productName), productName + " price");
	}

	public boolean isProductListed(String productName) {
		return isDisplayed(card(productName));
	}

	/* ------------------------------------------------------------------ */
	/* Cart actions                                                        */
	/* ------------------------------------------------------------------ */

	public ProductsPage addToCart(String productName) {
		click(addButton(productName), "Add to cart button for " + productName);
		return this;
	}

	public ProductsPage addToCart(List<String> productNames) {
		productNames.forEach(this::addToCart);
		Log.step("Added %d product(s) to the cart", productNames.size());
		return this;
	}

	public ProductsPage removeFromCart(String productName) {
		click(removeButton(productName), "Remove button for " + productName);
		return this;
	}

	/** True once the card shows "Remove", which is the UI's own idea of "in cart". */
	public boolean isInCart(String productName) {
		return isDisplayed(removeButton(productName));
	}

	public String cartButtonLabel(String productName) {
		return textOf(cartButtonWithinCard(productName), productName + " cart button");
	}

	/* ------------------------------------------------------------------ */
	/* Sorting                                                             */
	/* ------------------------------------------------------------------ */

	public ProductsPage sortBy(String visibleOption) {
		selectByVisibleText(SORT_DROPDOWN, visibleOption, "Sort dropdown");
		return this;
	}

	public String activeSortOption() {
		return selectedOption(SORT_DROPDOWN);
	}

	public List<String> sortOptions() {
		return dropdownOptions(SORT_DROPDOWN);
	}

	/* ------------------------------------------------------------------ */
	/* Navigation                                                          */
	/* ------------------------------------------------------------------ */

	public ProductDetailsPage openProduct(String productName) {
		click(nameLink(productName), productName + " product link");
		return new ProductDetailsPage();
	}

	/* ------------------------------------------------------------------ */
	/* Dynamic locators                                                    */
	/* ------------------------------------------------------------------ */

	private By card(String productName) {
		return By.xpath("//div[contains(@class,'inventory_item')]"
				+ "[.//div[contains(@class,'inventory_item_name')][normalize-space()="
				+ LocatorUtils.xpathLiteral(productName) + "]]");
	}

	private By nameLink(String productName) {
		return By.xpath("//div[contains(@class,'inventory_item_name')][normalize-space()="
				+ LocatorUtils.xpathLiteral(productName) + "]");
	}

	private By priceWithinCard(String productName) {
		return By.xpath(cardXpath(productName) + "//div[contains(@class,'inventory_item_price')]");
	}

	private By cartButtonWithinCard(String productName) {
		return By.xpath(cardXpath(productName) + "//button");
	}

	/**
	 * The application encodes the product name into the button id
	 * ({@code add-to-cart-sauce-labs-backpack}), which is a more stable hook
	 * than positional XPath when the catalogue is re-ordered.
	 */
	private By addButton(String productName) {
		return By.id("add-to-cart-" + LocatorUtils.slug(productName));
	}

	private By removeButton(String productName) {
		return By.id("remove-" + LocatorUtils.slug(productName));
	}

	private String cardXpath(String productName) {
		return "//div[contains(@class,'inventory_item')]"
				+ "[.//div[contains(@class,'inventory_item_name')][normalize-space()="
				+ LocatorUtils.xpathLiteral(productName) + "]]";
	}
}
