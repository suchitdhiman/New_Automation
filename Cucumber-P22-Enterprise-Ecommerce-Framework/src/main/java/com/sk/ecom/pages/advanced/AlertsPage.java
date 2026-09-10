package com.sk.ecom.pages.advanced;

import com.sk.ecom.utils.BrowserUtils;

import org.openqa.selenium.By;

/**
 * Native browser dialogs: alert, confirm and prompt.
 *
 * <p>These live outside the DOM, so no locator can reach them — every
 * interaction goes through {@code switchTo().alert()}, wrapped in
 * {@link BrowserUtils} with a wait so a dialog that opens a beat late does not
 * fail the test.
 */
public class AlertsPage extends AdvancedBasePage {

	private static final By ALERT_BUTTON = By.cssSelector("button[onclick='jsAlert()']");
	private static final By CONFIRM_BUTTON = By.cssSelector("button[onclick='jsConfirm()']");
	private static final By PROMPT_BUTTON = By.cssSelector("button[onclick='jsPrompt()']");
	private static final By RESULT = By.id("result");

	@Override
	public boolean isAt() {
		return isDisplayed(ALERT_BUTTON);
	}

	public AlertsPage open() {
		navigateTo("/javascript_alerts");
		return this;
	}

	public AlertsPage triggerAlert() {
		click(ALERT_BUTTON, "JS Alert button");
		return this;
	}

	public AlertsPage triggerConfirm() {
		click(CONFIRM_BUTTON, "JS Confirm button");
		return this;
	}

	public AlertsPage triggerPrompt() {
		click(PROMPT_BUTTON, "JS Prompt button");
		return this;
	}

	public String readAlertText() {
		return BrowserUtils.alertText();
	}

	public AlertsPage accept() {
		BrowserUtils.acceptAlert();
		return this;
	}

	public AlertsPage dismiss() {
		BrowserUtils.dismissAlert();
		return this;
	}

	public AlertsPage typeAndAccept(String text) {
		BrowserUtils.typeInAlertAndAccept(text);
		return this;
	}

	public String result() {
		return textOf(RESULT, "Result message");
	}
}
