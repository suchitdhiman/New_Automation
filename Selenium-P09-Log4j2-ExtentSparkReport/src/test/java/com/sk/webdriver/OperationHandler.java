package com.sk.webdriver;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.aventstack.extentreports.Status;

public class OperationHandler extends WebDriverManager {
	
	private static final Logger logger = Logger.getLogger(OperationHandler.class);

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
		} else if (locatorKey.endsWith("_xPath")) {
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
		//logger.info("Init the properties files.....");
		extentTest = extentReports.createTest("OperationHandler");
		extentTest.log(Status.INFO, "Init the properties files.....");

		browserLaunch("chrome");
		//logger.info("Launched the browser::"+browsProperties.getProperty("chrome"));
		extentTest.log(Status.INFO,"Launched the browser::"+browsProperties.getProperty("chrome") );

		selectUrl("amazon");
		//logger.info("Url selected::"+urlProperties.getProperty("amazon"));
		extentTest.log(Status.FAIL,"Url selected::"+urlProperties.getProperty("amazon") );


		webDriver.navigate().refresh();
		extentTest.log(Status.SKIP, "Skiped! Not tracked");

		selectOption("amazondropbox_id", "Books");
		//logger.info("Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		extentTest.log(Status.PASS, "Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		
		typeText("amazonsearchtextbox_name", "Harry Potter");
		//logger.info("Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		extentTest.log(Status.PASS, "Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		
		clickElement("amazonsearchbutton_xPath");
		//logger.info("Clicked on button by locating the locator::"+orProperties.getProperty("amazonsearchbutton_xPath"));
		extentTest.log(Status.PASS, "Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));

		extentReports.flush();
		
		webDriver.quit();
	}

}
