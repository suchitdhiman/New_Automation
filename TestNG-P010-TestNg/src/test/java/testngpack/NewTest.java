package testngpack;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;


import com.aventstack.extentreports.Status;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

public class NewTest extends BaseClass{
	
  @Test
  public void actualTest() {
	  System.out.println("NewTest.actualTest():Starts");
	  selectOption("amazondropbox_id", "Books");
		//logger.info("Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		extentTest.log(Status.PASS, "Selected the option <Books> by locating the locator::"+orProperties.getProperty("amazondropbox_id"));
		
		textType("amazonsearchtextbox_name", "Harry Potter");
		//logger.info("Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		extentTest.log(Status.PASS, "Typed the text <Harry Potter> by locating the locator::"+orProperties.getProperty("amazonsearchtextbox_name"));
		
		clickElement("amazonsearchbutton_xPath");
		//logger.info("Clicked on button by locating the locator::"+orProperties.getProperty("amazonsearchbutton_xPath"));
		extentTest.log(Status.PASS, "Clicked on the button"+orProperties.getProperty("amazonsearchbutton_xPath"));
  }
  
  @BeforeMethod
  @Parameters("browser")
  public void beforeMethod(String browser) throws Exception {
	  	init();
		//logger.info("Init the properties files.....");
	  	extentTest = extentReports.createTest("OperationHandler");
	  	extentTest.log(Status.INFO, "Init the properties files....");
		
		browserLauncher(browser);
		//logger.info("Launched the browser::"+browsProperties.getProperty("chrome"));
		extentTest.log(Status.INFO,"Launched the browser::"+browsproperties.getProperty(browser) );
		
		selectUrl("amazon");
		extentTest.log(Status.FAIL, "url selected::"+urlProperties.getProperty("amazon"));
		
		webDriver.navigate().refresh();
		extentTest.log(Status.SKIP, "Skiped! Not tracked");
		
		System.out.println("NewTest.beforeMethod()");
	  
  }

  @AfterMethod
  public void afterMethod() {
	  extentReports.flush();
	  webDriver.quit();
	  System.out.println("NewTest.afterMethod()");
  }

  @BeforeClass
  public void beforeClass() {
	  System.out.println("NewTest.beforeClass()");
  }

  @AfterClass
  public void afterClass() {
	  System.out.println("NewTest.afterClass()");
	  
  }

  @BeforeTest
  public void beforeTest() {
	  System.out.println("NewTest.beforeTest()");
  }

  @AfterTest
  public void afterTest() {
	  System.out.println("NewTest.afterTest()");
  }

  @BeforeSuite
  public void beforeSuite() {
	  System.out.println("NewTest.beforeSuite()");
  }

  @AfterSuite
  public void afterSuite() {
	  System.out.println("NewTest.afterSuite()");
  }

}
