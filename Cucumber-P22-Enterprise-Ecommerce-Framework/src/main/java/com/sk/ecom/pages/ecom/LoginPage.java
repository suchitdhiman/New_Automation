package com.sk.ecom.pages.ecom;

import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.enums.ConfigKey;
import com.sk.ecom.enums.WaitStrategy;
import com.sk.ecom.logging.Log;
import com.sk.ecom.model.User;
import com.sk.ecom.pages.base.BasePage;

import org.openqa.selenium.By;

/**
 * Sign-in screen — the entry point for every UI scenario.
 *
 * <p>Locators are {@code private static final By} constants rather than
 * {@code @FindBy} fields: a {@code By} is re-resolved on every use, which
 * removes a whole class of {@code StaleElementReferenceException} on screens
 * that re-render, and it can be passed to the wait-aware helpers in
 * {@link BasePage}.
 */
public class LoginPage extends BasePage {

	private static final By USERNAME = By.id("user-name");
	private static final By PASSWORD = By.id("password");
	private static final By LOGIN_BUTTON = By.id("login-button");
	private static final By ERROR_MESSAGE = By.cssSelector("h3[data-test='error']");
	private static final By ERROR_CLOSE = By.cssSelector("button.error-button");
	private static final By LOGO = By.cssSelector(".login_logo");

	@Override
	public boolean isAt() {
		return isDisplayed(LOGIN_BUTTON);
	}

	public LoginPage open() {
		openUrl(ConfigManager.get(ConfigKey.APP_BASE_URL));
		Log.step("Opened the storefront sign-in page");
		return this;
	}

	public LoginPage enterUsername(String username) {
		type(USERNAME, username, "Username field");
		return this;
	}

	public LoginPage enterPassword(String password) {
		type(PASSWORD, password, "Password field");
		return this;
	}

	/** @return the catalogue page; the caller asserts it actually arrived. */
	public ProductsPage submit() {
		click(LOGIN_BUTTON, "Login button");
		return new ProductsPage();
	}

	/** The happy path in one call, for use as a Background step. */
	public ProductsPage loginAs(User user) {
		Log.step("Signing in as [%s] (%s)", user.username(), user.description());
		return enterUsername(user.username()).enterPassword(user.password()).submit();
	}

	/** For negative scenarios where the login is expected to be rejected. */
	public LoginPage attemptLogin(String username, String password) {
		enterUsername(username);
		enterPassword(password);
		click(LOGIN_BUTTON, "Login button");
		return this;
	}

	public boolean hasError() {
		return isDisplayed(ERROR_MESSAGE);
	}

	public String errorMessage() {
		return textOf(ERROR_MESSAGE, "Login error banner");
	}

	public LoginPage dismissError() {
		click(ERROR_CLOSE, "Error banner close button");
		return this;
	}

	public boolean isLogoVisible() {
		return isDisplayed(LOGO, java.time.Duration.ofSeconds(5));
	}

	/** True when the field is flagged invalid, which is styling rather than text. */
	public boolean isFieldHighlightedAsInvalid(String field) {
		By locator = switch (field.toLowerCase()) {
			case "username" -> USERNAME;
			case "password" -> PASSWORD;
			default -> throw new IllegalArgumentException("Unknown login field: " + field);
		};
		String cssClass = attributeOf(locator, "class", field + " field");
		return cssClass != null && cssClass.contains("error");
	}

	public LoginPage waitUntilReady() {
		find(LOGIN_BUTTON, WaitStrategy.CLICKABLE);
		return this;
	}
}
