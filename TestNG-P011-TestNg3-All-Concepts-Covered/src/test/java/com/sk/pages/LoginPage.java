package com.sk.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.sk.model.TestUser;

/**
 * The Swag Labs sign-in screen.
 *
 * <p>Two entry points on purpose:
 * <ul>
 *   <li>{@link #loginAs(TestUser)} returns the next page, so a happy-path test
 *       reads as a chain and a broken login fails right here rather than three
 *       assertions later.</li>
 *   <li>{@link #loginExpectingFailure(String, String)} returns {@code this},
 *       because a negative test wants to stay on the login page and read the
 *       error banner.</li>
 * </ul>
 * Returning the correct page object from an action is the part of POM that
 * actually earns its keep.
 */
public class LoginPage extends BasePage {

	private static final By USERNAME = By.id("user-name");
	private static final By PASSWORD = By.id("password");
	private static final By LOGIN_BUTTON = By.id("login-button");
	private static final By ERROR_BANNER = By.cssSelector("h3[data-test=\"error\"]");
	private static final By ERROR_CLOSE = By.cssSelector("button.error-button");
	private static final By CREDENTIALS_PANEL = By.cssSelector("[data-test=\"login-credentials\"]");

	public LoginPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public boolean isLoaded() {
		return isPresent(LOGIN_BUTTON);
	}

	public ProductsPage loginAs(TestUser user) {
		return loginAs(user.username(), user.password());
	}

	public ProductsPage loginAs(String username, String password) {
		submit(username, password);
		ProductsPage products = new ProductsPage(driver);
		products.assertLoadedOrExplain();
		return products;
	}

	/** Fills the form and stays put, whatever the application decides to do. */
	public LoginPage loginExpectingFailure(String username, String password) {
		submit(username, password);
		return this;
	}

	private void submit(String username, String password) {
		log.info("Signing in as [{}]", username);
		type(USERNAME, username);
		type(PASSWORD, password);
		click(LOGIN_BUTTON);
	}

	public boolean isErrorDisplayed() {
		return isPresent(ERROR_BANNER);
	}

	public String errorMessage() {
		return textOf(ERROR_BANNER);
	}

	public LoginPage dismissError() {
		if (isPresent(ERROR_CLOSE)) {
			click(ERROR_CLOSE);
		}
		return this;
	}

	/** The demo site prints its own accepted usernames - handy as a self-check. */
	public boolean showsPublishedCredentials() {
		return isPresent(CREDENTIALS_PANEL);
	}
}
