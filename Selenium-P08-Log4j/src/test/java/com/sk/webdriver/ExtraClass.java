package com.sk.webdriver;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

public class ExtraClass extends WebDriverManager {

    private static void selectOption(String locatorKey, String text) {
    	
    	
        String locator = orProperties.getProperty(locatorKey);
        
        System.out.println(locator);

        Select dropdown = new Select(webDriver.findElement(By.id(locator)));
        dropdown.selectByVisibleText(text);
    }

    private static void typeText(String locatorKey, String text) {
        String locator = orProperties.getProperty(locatorKey);
        System.out.println(locator);

        webDriver.findElement(By.name(locator)).sendKeys(text);
    }

    private static void clickElement(String locatorKey) {
        String locator = orProperties.getProperty(locatorKey);
        System.out.println(locator);

        webDriver.findElement(By.xpath(locator)).click();
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        init();
        browserLaunch("chrome");
        selectUrl("amazon");

        webDriver.navigate().refresh();

        System.out.println("Current page:: " + webDriver.getTitle());

        selectOption("amazondropbox_id", "Books");

        typeText("amazonsearchtextbox_name", "Harry Potter");

        clickElement("amazonsearchbutton_xPath");
    }
}