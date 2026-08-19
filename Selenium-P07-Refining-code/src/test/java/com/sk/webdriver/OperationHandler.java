package com.sk.webdriver;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OperationHandler extends WebDriverManager {

	public static WebElement webElement = null;

	private static void selectOption(String locatorKey, String text) {

		// String locator = orProperties.getProperty(locatorKey);

		// System.out.println(locatorKey);

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.selectOption(Done)");

		// webElement = webDriver.findElement(By.id(locator));

		// webElement.sendKeys(text);

		// webDriver.findElement(By.id(locatorKey)).sendKeys(text);
	}

	private static void typeText(String locatorKey, String text) {

		String locator = orProperties.getProperty(locatorKey);
		System.out.println(locator);

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.typeText(Done)");

		// webDriver.findElement(By.name(locator)).sendKeys(text);

		// webDriver.findElement(By.name(locateKey)).sendKeys(text);
	}

	private static void clickElement(String locatorkey) {

		String locator = orProperties.getProperty(locatorkey);

		System.out.println(locator);

		getLocatorType(locatorkey).click();
		
		

		// webDriver.findElement(By.xpath(orProperties.getProperty(locatorkey))).click();

		// webDriver.findElement(By.xpath(locateKey)).click();
	}

	private static WebElement getLocatorType(String locatorKey) {

		if(!isElementPresent(locatorKey)) {
			System.out.println("Element is not present");
		}

		if (locatorKey.endsWith("_id")) {
			webElement = webDriver.findElement(By.id(orProperties.getProperty(locatorKey)));
		} else if (locatorKey.endsWith("_className")) {
			webElement = webDriver.findElement(By.className(orProperties.getProperty(locatorKey)));
		} else if (locatorKey.endsWith("_cssSelector")) {
			webElement = webDriver.findElement(By.cssSelector(orProperties.getProperty(locatorKey)));
		} else if (locatorKey.endsWith("_linkText")) {
			webElement = webDriver.findElement(By.linkText(orProperties.getProperty(locatorKey)));
		} else if (locatorKey.endsWith("_partialLinkText")) {
			webElement = webDriver.findElement(By.partialLinkText(orProperties.getProperty(locatorKey)));
		} else if (locatorKey.endsWith("_xPath")) {
			webElement = webDriver.findElement(By.xpath(orProperties.getProperty(locatorKey)));
		}else if (locatorKey.endsWith("_name")) {
			webElement = webDriver.findElement(By.name(orProperties.getProperty(locatorKey)));
		}
		return webElement;
	}

	private static boolean isElementPresent(String locatorKey) {
		
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorKey)));
			return true;
		}catch (Exception e) {
			return false;
		}
	}
		
/*
		if (locatorKey.endsWith("_id")) {
			webDriver.findElement(By.id(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_className")) {
			webDriver.findElement(By.className(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_cssSelector")) {
			webDriver.findElement(By.cssSelector(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_linkText")) {
			webDriver.findElement(By.linkText(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_name")) {
			webDriver.findElement(By.name(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_xpath")) {
			webDriver.findElement(By.xpath(orProperties.getProperty(locatorKey)));
			return true;
		}else if(locatorKey.endsWith("_partialLinkText")) {
			webDriver.findElement(By.partialLinkText(orProperties.getProperty(locatorKey)));
			return true;
		}else
			return false;
	}
	
	*/
	
	public static By getLocator(String locatorKey ) {
		
		By by = null;
		
		if(locatorKey.endsWith("_id")) {
			by= By.id(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_name")) {
			by=By.name(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_className")) {
			by=By.className(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_linkText")) {
			by=By.linkText(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_xpath")) {
			by=By.xpath(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_tagName")) {
			by=By.tagName(orProperties.getProperty(locatorKey));
		}else if(locatorKey.endsWith("_parialLinkText")) {
			by=By.partialLinkText(orProperties.getProperty(locatorKey));
		}
		return by;
		
	}
	

	public static void main(String[] args) throws InterruptedException, IOException {

		init();
		browserLaunch("chrome");
		selectUrl("amazon");

		webDriver.navigate().refresh();

		Thread.sleep(2000);
		System.out.println("Current page:: " + webDriver.getTitle());
		Thread.sleep(2000);

		selectOption("amazondropbox_id", "Books");

		Thread.sleep(2000);

		typeText("amazonsearchtextbox_name", "Harry Potter");

		Thread.sleep(2000);
		clickElement("amazonsearchbutton_xPath");

		/*
		 * selectOption("searchDropdownBox","Books");
		 * 
		 * 
		 * typeText("field-keywords", "Harry Potter");
		 * 
		 * clickElement("//*[@id=\"nav-search-submit-button\"]");
		 */

	}

}
