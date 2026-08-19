package com.sk.webdriver;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OperationHandler extends WebDriverManager {

	private static void selectOption(String locatorKey, String text) {

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.selectOption(Done)");

	}

	private static void typeText(String locatorKey, String text) {

		String locator = orProperties.getProperty(locatorKey);
		
		System.out.println(locator);

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.typeText(Done)");

	}

	private static void clickElement(String locatorkey) {

		String locator = orProperties.getProperty(locatorkey);

		System.out.println(locator);

		getLocatorType(locatorkey).click();

	}

	private static WebElement getLocatorType(String locatorKey) {

		WebElement webElement = null;

		if (!isElementPresent(locatorKey)) {
			
		System.out.println("Element is not present");
		
		}

		webElement = webDriver.findElement(getLocator(locatorKey));

		return webElement;
	}

	private static boolean isElementPresent(String locatorKey) {

		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorKey)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static By getLocator(String locatorKey) {

		By by = null;

		if (locatorKey.endsWith("_id")) {
			by = By.id(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_name")) {
			by = By.name(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_className")) {
			by = By.className(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_linkText")) {
			by = By.linkText(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_xpath")) {
			by = By.xpath(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_tagName")) {
			by = By.tagName(orProperties.getProperty(locatorKey));
		} else if (locatorKey.endsWith("_parialLinkText")) {
			by = By.partialLinkText(orProperties.getProperty(locatorKey));
		}
		return by;

	}

	public static void main(String[] args) throws InterruptedException, IOException {

		init();

		browserLaunch("chrome");

		selectUrl("amazon");

		webDriver.navigate().refresh();

		selectOption("amazondropbox_id", "Books");

		typeText("amazonsearchtextbox_name", "Harry Potter");

		clickElement("amazonsearchbutton_xPath");

	}

}
