package com.sk.ecom.pages.components;

import com.sk.ecom.enums.WaitStrategy;
import com.sk.ecom.logging.Log;
import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.pages.ecom.LoginPage;
import com.sk.ecom.pages.ecom.ProductsPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * The slide-out navigation drawer.
 *
 * <p>An animated drawer is the classic false-positive generator: the links exist
 * in the DOM before the panel has finished sliding in, so an unguarded click
 * lands on nothing. {@link #waitUntilOpen()} waits for the links to be
 * <em>clickable</em>, not merely present, which is the difference between a
 * suite that passes on a fast laptop and one that also passes on a loaded CI
 * agent.
 */
public class SideMenuComponent extends BasePage {

	@FindBy(id = "inventory_sidebar_link")
	private WebElement allItemsLink;

	@FindBy(id = "about_sidebar_link")
	private WebElement aboutLink;

	@FindBy(id = "logout_sidebar_link")
	private WebElement logoutLink;

	@FindBy(id = "reset_sidebar_link")
	private WebElement resetAppStateLink;

	private static final By CLOSE_BUTTON = By.id("react-burger-cross-btn");
	private static final By LOGOUT = By.id("logout_sidebar_link");
	private static final By MENU_WRAPPER = By.cssSelector(".bm-menu-wrap");

	public SideMenuComponent() {
		PageFactory.initElements(driver(), this);
	}

	@Override
	public boolean isAt() {
		return isDisplayed(LOGOUT, java.time.Duration.ofSeconds(5));
	}

	public SideMenuComponent waitUntilOpen() {
		find(LOGOUT, WaitStrategy.CLICKABLE);
		return this;
	}

	public boolean isOpen() {
		String hidden = attributeOf(MENU_WRAPPER, "aria-hidden", "Menu wrapper");
		return !"true".equals(hidden);
	}

	public ProductsPage goToAllItems() {
		allItemsLink.click();
		return new ProductsPage();
	}

	/** Opens the corporate site in the same tab; returns the destination URL. */
	public String goToAbout() {
		aboutLink.click();
		return currentUrl();
	}

	public LoginPage logout() {
		logoutLink.click();
		Log.step("Signed out via the side menu");
		return new LoginPage();
	}

	/** Clears the cart and re-enables every add-to-cart button. */
	public SideMenuComponent resetAppState() {
		resetAppStateLink.click();
		Log.step("Reset application state");
		return this;
	}

	public void close() {
		click(CLOSE_BUTTON, "Close menu button");
	}
}
