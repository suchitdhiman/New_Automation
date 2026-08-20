package com.sk.webdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;

//import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.sk.frames.ExtentManager;


public class BaseClass {
	
	public static WebDriver webDriver;
    public static Properties browsProperties;
    public static Properties urlProperties;
    public static Properties orProperties;
    public static WebDriverWait wait;
    public static Properties logProperties;
    public static ExtentReports extentReports;
    public static ExtentTest extentTest;
    
   // private static final Logger logger = Logger.getLogger(OperationHandler.class);

    public static void init() throws IOException {

        String defaultPath = System.getProperty("user.dir");

        // Browser properties
        browsProperties = new Properties();
        FileInputStream fis1 = new FileInputStream(
                new File(defaultPath + "\\src\\test\\resources\\browser.properties"));
        browsProperties.load(fis1);

        // URL properties
        urlProperties = new Properties();
        FileInputStream fis2 = new FileInputStream(
                new File(defaultPath + "\\src\\test\\resources\\url.properties"));
        urlProperties.load(fis2);

        // OR properties
        orProperties = new Properties();
        FileInputStream fis3 = new FileInputStream(
                new File(defaultPath + "\\src\\test\\resources\\or.properties"));
        orProperties.load(fis3);
        
        // log4j properties
        FileInputStream logFileInputStream = new FileInputStream(defaultPath+"\\src\\test\\resources\\log4jConfig.properties");
        PropertyConfigurator.configure(logFileInputStream);
        
        //ExtendReports
       extentReports = ExtentManager.getInstance();
        
    }

    public static void browserLaunch(String browser) {

        if (browser.equalsIgnoreCase("chrome")) {
            webDriver = new ChromeDriver();
        } else if (browser.equalsIgnoreCase("edge")) {
            webDriver = new EdgeDriver();
        } else if (browser.equalsIgnoreCase("firefox")) {
            webDriver = new FirefoxDriver();
        } else {
            throw new RuntimeException("Invalid browser: " + browser);
        }

        webDriver.manage().window().maximize();
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
    }

    public static void selectUrl(String site) {
        webDriver.get(urlProperties.getProperty(site));
    }
    
    public static void selectOption(String locatorKey, String text) {

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.selectOption(Done)");

	}

	public static void typeText(String locatorKey, String text) {

		String locator = orProperties.getProperty(locatorKey);
		
		System.out.println(locator);

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.typeText(Done)");

	}

	public static void clickElement(String locatorkey) {

		String locator = orProperties.getProperty(locatorkey);

		System.out.println(locator);

		getLocatorType(locatorkey).click();

	}
	
	public static String getText(String locatorKey) {
		return getLocatorType(locatorKey).getText();
	}

	public static WebElement getLocatorType(String locatorKey) {

		WebElement webElement = null;

		if (!isElementPresent(locatorKey)) {
			
		System.out.println("Element is not present");
		
		}

		webElement = webDriver.findElement(getLocator(locatorKey));

		return webElement;
	}

	public static boolean isElementPresent(String locatorKey) {

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
	
	public static boolean isLinkEqual(String expectedLink) {
		String actualLink = getText("amazon_linkText");
		if(actualLink.equals(expectedLink)) {
			return true;
		}else {
			return false;
		}
	}
	
	public static void reportSuccess(String scuessMessage) {
		extentTest.log(Status.PASS, scuessMessage);
	}
	
	public static void reportFailure(String failureMessage, WebElement element) throws IOException {
		extentTest.log(Status.FAIL, failureMessage);
		if(element !=null) {
			takesScreenshot(element);
		}
	}
	
	private static void takesScreenshot(WebElement element) throws IOException{
		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy -MM-dd_HH-mm-ss");
		String dateFormat = simpleDateFormat.format(date) + ".png";
		
		String screenshotDir = System.getProperty("user.dir") + "/failurescreenshots/";
		
		//create directory with validation
		File dir = new File(screenshotDir);
		if(!dir.exists()) {
			boolean created = dir.mkdirs();
			if(!created) {
				throw new IOException("Failed to create screenshot directory: "+screenshotDir);
			}
		}
		drawBorder(webDriver, element);
		
		File scrFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
		String screenshotPath = screenshotDir + dateFormat;
		
		FileHandler.copy(scrFile, new File(screenshotPath));
		extentTest.log(Status.INFO, "Screenhot -->" +extentTest.addScreenCaptureFromPath(screenshotPath));	
	}
	
	public static void drawBorder(WebDriver webDriver, WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor)webDriver;
		js.executeScript("arguments[0].style.border='5px solid yellow'", element);
	}

}
