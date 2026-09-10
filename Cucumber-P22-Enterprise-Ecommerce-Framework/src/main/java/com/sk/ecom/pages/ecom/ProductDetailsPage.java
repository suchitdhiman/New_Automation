package com.sk.ecom.pages.ecom;

import com.sk.ecom.model.Product;
import com.sk.ecom.pages.base.BasePage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Single product view.
 *
 * <p>Deliberately written with {@code @FindBy} + {@link PageFactory} rather than
 * {@code By} constants, so the project demonstrates both styles side by side.
 *
 * <p>Trade-off worth knowing: {@code @FindBy} proxies are lazy but cache the
 * located element, so on a screen that re-renders they are the usual source of
 * {@code StaleElementReferenceException}. This page is static once loaded, which
 * is exactly where the annotation style is safe — the rest of the suite uses
 * {@code By} constants.
 */
public class ProductDetailsPage extends BasePage {

	@FindBy(css = ".inventory_details_name")
	private WebElement productName;

	@FindBy(css = ".inventory_details_desc")
	private WebElement productDescription;

	@FindBy(css = ".inventory_details_price")
	private WebElement productPrice;

	@FindBy(id = "back-to-products")
	private WebElement backButton;

	@FindBy(css = ".inventory_details_img")
	private WebElement productImage;

	private static final By ADD_TO_CART = By.cssSelector("button.btn_inventory[id^='add-to-cart']");
	private static final By REMOVE_FROM_CART = By.cssSelector("button.btn_inventory[id^='remove']");

	public ProductDetailsPage() {
		PageFactory.initElements(driver(), this);
	}

	@Override
	public boolean isAt() {
		return currentUrl().contains("inventory-item.html")
				&& isDisplayed(By.cssSelector(".inventory_details_name"));
	}

	public String name() {
		return productName.getText().trim();
	}

	public String description() {
		return productDescription.getText().trim();
	}

	public String price() {
		return productPrice.getText().trim();
	}

	public boolean hasImage() {
		String src = productImage.getDomAttribute("src");
		return src != null && !src.isBlank();
	}

	/** @return the product exactly as this screen presents it, for comparison with the list view. */
	public Product asProduct() {
		return new Product(name(), description(), price());
	}

	public ProductDetailsPage addToCart() {
		click(ADD_TO_CART, "Add to cart button");
		return this;
	}

	public ProductDetailsPage removeFromCart() {
		click(REMOVE_FROM_CART, "Remove button");
		return this;
	}

	public boolean isInCart() {
		return isDisplayed(REMOVE_FROM_CART);
	}

	public ProductsPage goBackToCatalogue() {
		backButton.click();
		return new ProductsPage();
	}
}
