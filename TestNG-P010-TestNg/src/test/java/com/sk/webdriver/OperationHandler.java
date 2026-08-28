package com.sk.webdriver;

import com.aventstack.extentreports.Status;

public class OperationHandler extends BaseClass {
	
	//private static final Logger logger = Logger.getLogger(OperationHandler.class);

	
	public static void main(String[] args) throws Exception {
		init();
		//logger.info("Init the properties files.....");
		extentTest = extentReports.createTest("OperationHandler");
		extentTest.log(Status.INFO, "Init the properties files.....");

		browserLauncher();
		//logger.info("Launched the browser::"+browsProperties.getProperty("chrome"));
		extentTest.log(Status.INFO,"Launched the browser::"+browsproperties.getProperty("chrome") );

		selectUrl("amazon");
		//logger.info("Url selected::"+urlProperties.getProperty("amazon"));
		extentTest.log(Status.FAIL,"Url selected::"+urlProperties.getProperty("amazon") );


		webDriver.navigate().refresh();
		extentTest.log(Status.SKIP, "Skiped! Not tracked");

		selectOption("amazondropbox_id", "Books");
		//logger.info("Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		extentTest.log(Status.PASS, "Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		
		textType("amazonsearchtextbox_name", "Harry Potter");
		//logger.info("Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		extentTest.log(Status.PASS, "Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		
		clickElement("amazonsearchbutton_xPath");
		//logger.info("Clicked on button by locating the locator::"+orProperties.getProperty("amazonsearchbutton_xPath"));
		extentTest.log(Status.PASS, "Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));

		extentReports.flush();
		
		webDriver.quit();
	}

}
