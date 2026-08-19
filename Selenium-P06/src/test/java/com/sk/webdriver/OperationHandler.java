package com.sk.webdriver;

import java.io.IOException;

import org.openqa.selenium.By;


public class OperationHandler extends WebDriverManager{
	
	
	private static void clickElement(String locateKey) {
		webDriver.findElement(By.xpath(locateKey)).click();
		
		
	}

	private static void typeText(String locateKey, String text) {
		webDriver.findElement(By.name(locateKey)).sendKeys(text);
		
	}

	private static void selectOption(String locatorKey, String text) {
		webDriver.findElement(By.id(locatorKey)).sendKeys(text);
		
		
	}
	
	

	public static void main(String[] args) throws InterruptedException {
		
		try {
			intit();
			browserLaunch("chrome");
			selectUrl("amazon");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
		webDriver.navigate().refresh();
		
		Thread.sleep(2000);
		System.out.println("Current page:: " + webDriver.getTitle());
		Thread.sleep(2000);
		
		selectOption("searchDropdownBox","Books");
		
	
		typeText("field-keywords", "Harry Potter");
		
		clickElement("//*[@id=\"nav-search-submit-button\"]");
		
		

	}



}
