package com.sk.webdriver;

import java.io.IOException;

import org.openqa.selenium.WebElement;

//import org.apache.log4j.Logger;

import com.aventstack.extentreports.Status;

public class Verification extends BaseClass {
	// private static final Logger logger = Logger.getLogger(OperationHandler.class);
	

	public static void main(String[] args) throws IOException, InterruptedException {
		
		init();
		//logger.info("Init the properties files.....");
		extentTest = extentReports.createTest("Verification");
		extentTest.log(Status.INFO, "Init the properties files.....");

		browserLaunch("chrome");
		//logger.info("Launched the browser::"+browsProperties.getProperty("chrome"));
		extentTest.log(Status.INFO,"Launched the browser::"+browsProperties.getProperty("chrome") );
		selectUrl("amazon");
		
		//logger.info("Url selected::"+urlProperties.getProperty("amazon"));
		extentTest.log(Status.INFO,"Url selected::"+urlProperties.getProperty("amazon") );

		Thread.sleep(3000);
		
		webDriver.navigate().refresh();
		
		WebElement element = getLocatorType("amazon_linkText");
		String actualLink = getText("amazon_linkText");
		//String actualLink = element.getText();
		
		String expectedLink = "today's Deals";
		System.out.println("Actual Link: "+actualLink);
		System.out.println("Expected Link: "+expectedLink);
		
		if(!isLinkEqual(expectedLink)) {
			reportFailure("Both the links are not equal.....", element);
		}else {
			reportSuccess("Both the links are equal....");
		}
		
		
		extentReports.flush();
		
		webDriver.quit();

	}

}
