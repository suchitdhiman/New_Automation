package com.sk.ecom.pages.components;

import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.pages.ecom.CartPage;

import org.openqa.selenium.By;

/**
 * The persistent header — cart badge, cart link and burger menu.
 *
 * <p>Component Object rather than a page: the header exists on the catalogue,
 * the cart and both checkout steps. Modelling it once stops the cart-count
 * locator from being copy-pasted into four page classes, which is exactly the
 * duplication that makes a UI change expensive.
 */
public class HeaderComponent extends BasePage {

	private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
	private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");
	private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
	private static final By PAGE_TITLE = By.cssSelector("span.title");
	private static final By APP_LOGO = By.cssSelector(".app_logo");

	@Override
	public boolean isAt() {
		return isDisplayed(CART_LINK);
	}

	/**
	 * @return the number on the cart badge, or {@code 0} when there is no badge
	 *         — the application removes the element entirely when the cart is
	 *         empty, so "absent" and "zero" are the same fact.
	 */
	public int cartCount() {
		if (!isDisplayed(CART_BADGE, java.time.Duration.ofSeconds(2))) {
			return 0;
		}
		return Integer.parseInt(textOf(CART_BADGE, "Cart badge"));
	}

	public boolean isCartBadgeVisible() {
		return isDisplayed(CART_BADGE, java.time.Duration.ofSeconds(2));
	}

	public CartPage openCart() {
		click(CART_LINK, "Cart icon");
		return new CartPage();
	}

	public SideMenuComponent openMenu() {
		click(MENU_BUTTON, "Burger menu button");
		return new SideMenuComponent().waitUntilOpen();
	}

	public String pageTitle() {
		return textOf(PAGE_TITLE, "Page heading");
	}

	public String appName() {
		return textOf(APP_LOGO, "Application logo");
	}
}
