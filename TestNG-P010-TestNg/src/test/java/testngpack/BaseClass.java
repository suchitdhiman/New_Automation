package testngpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;

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

public class BaseClass {
	public static WebDriver webDriver;
	public static WebElement webElement;
	public static Properties browsproperties;
	public static Properties urlProperties;
	public static Properties orProperties;
	public static WebDriverWait wait;
	public static ExtentReports extentReports;
	public static ExtentTest extentTest;
	
	
	//Default path 
	public static String defaultPath = System.getProperty("user.dir");
	
	
	public static void init() throws Exception {
		//Browser properties
		browsproperties = new Properties();
		File browsFile = 
				new File(defaultPath + "\\src\\test\\resources\\browser.properties");		
		FileInputStream fileInputStreamp = 
				new FileInputStream(browsFile);
		browsproperties.load(fileInputStreamp);
		
		//url file properties
		urlProperties = new Properties();
		File urlFile  = 
				new File(defaultPath+"\\src\\test\\resources\\url.properties");
		FileInputStream fileInputStreamurl = 
				new FileInputStream(urlFile);
		urlProperties.load(fileInputStreamurl);
		
		//or properties
		orProperties = new Properties();
		File orFile = 
				new File(defaultPath+"\\src\\test\\resources\\or.properties");
		FileInputStream fileInputStreamOR = 
				new FileInputStream(orFile);
		orProperties.load(fileInputStreamOR);
		
		//log4j properties
		File logFile = 
				new File(defaultPath+"\\src\\test\\resources\\log4jConfig.properties");
		FileInputStream fileInputStreamLog = 
				new FileInputStream(logFile);
		PropertyConfigurator.configure(fileInputStreamLog);
		
		//extend reports
		extentReports = ExtentManager.getInstance();
	}
	
	public static void browserLauncher(String brows) throws Exception {
		//String brows = browsproperties.getProperty("browser");
		if(brows.equalsIgnoreCase("chrome")) {
			webDriver = new ChromeDriver();
		}else if(brows.equalsIgnoreCase("edge")) {
			webDriver = new EdgeDriver();
		}else if(brows.equalsIgnoreCase("firfox")) {
			webDriver = new FirefoxDriver();
		}else {
			throw new Exception("browser not available");
		}
		System.out.println("Test1:: Done");
		webDriver.manage().window().maximize();
		wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
	}
	
	public static void selectUrl(String urlkey) {
		//String url = urlProperties.getProperty(urlkey);
		webDriver.get(urlProperties.getProperty(urlkey));
	}
	
	public static void selectOption(String locatorKey, String text) {

		getLocatorType(locatorKey).sendKeys(text);

		System.out.println("OperationHandler.selectOption(Done)");

	}

	public static void textType(String locatorKey, String text) {

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
	
	public static void selectFrame(String frameName) {
		webDriver.switchTo().frame(webDriver.findElement(By.name(frameName)));
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
