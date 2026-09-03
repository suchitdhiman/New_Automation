package com.sk.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class BaseClass {

    // ---- PER-THREAD state: each parallel thread gets its own instance ----
    private static ThreadLocal<WebDriver> webDriverTL = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> waitTL = new ThreadLocal<>();
    private static ThreadLocal<ExtentTest> extentTestTL = new ThreadLocal<>();

    // ---- SHARED, read-only after init(): safe as static ----
    public static Properties browsproperties;
    public static Properties urlProperties;
    public static Properties orProperties;
    public static ExtentReports extentReports;

    public static String defaultPath = System.getProperty("user.dir");

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(BaseClass.class);

    // ---- Accessors: use these everywhere instead of the old fields ----
    public static WebDriver getDriver() {
        return webDriverTL.get();
    }

    public static WebDriverWait getWait() {
        return waitTL.get();
    }

    public static ExtentTest getExtentTest() {
        return extentTestTL.get();
    }

    public static void setExtentTest(ExtentTest test) {
        extentTestTL.set(test);
    }

    /**
     * Initialize all properties files. Call ONCE from @BeforeSuite.
     */
    public static synchronized void init() throws Exception {
        if (browsproperties != null) {
            return; // already initialized
        }
        logger.info("Initializing properties files...");

        browsproperties = new Properties();
        File browsFile = new File(defaultPath + "\\src\\test\\resources\\browser.properties");
        try (FileInputStream fis = new FileInputStream(browsFile)) {
            browsproperties.load(fis);
        }
        logger.info("Browser properties loaded");

        urlProperties = new Properties();
        File urlFile = new File(defaultPath + "\\src\\test\\resources\\url.properties");
        try (FileInputStream fis = new FileInputStream(urlFile)) {
            urlProperties.load(fis);
        }
        logger.info("URL properties loaded");

        orProperties = new Properties();
        File orFile = new File(defaultPath + "\\src\\test\\resources\\or.properties");
        try (FileInputStream fis = new FileInputStream(orFile)) {
            orProperties.load(fis);
        }
        logger.info("Object Repository properties loaded");

        File logFile = new File(defaultPath + "\\src\\test\\resources\\log4jConfig.properties");
        try (FileInputStream fis = new FileInputStream(logFile)) {
            PropertyConfigurator.configure(fis);
        }
        logger.info("Log4j configured");

        extentReports = ExtentManager.getInstance();
        logger.info("Extent Reports initialized");
    }

    /**
     * Launch browser and maximize window — fresh instance PER THREAD.
     */
    public static void browserLauncher() throws Exception {
        String brows = browsproperties.getProperty("browser");
        logger.info("Launching browser: " + brows);

        WebDriver driver;
        if (brows.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (brows.equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();
        } else if (brows.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else {
            throw new Exception("Browser not available: " + brows);
        }

        driver.manage().window().maximize();
        webDriverTL.set(driver);
        waitTL.set(new WebDriverWait(driver, Duration.ofSeconds(10)));
        logger.info("Browser launched and configured");
    }

    /**
     * Quit browser for THIS thread only, and clear ThreadLocal refs.
     */
    public static void quitBrowser() {
        WebDriver driver = webDriverTL.get();
        if (driver != null) {
            driver.quit();
            webDriverTL.remove();
            waitTL.remove();
        }
    }

    public static void selectUrl(String urlkey) {
        String url = urlProperties.getProperty(urlkey);
        logger.info("Navigating to URL: " + url);
        getDriver().get(url);
    }

    public static void typeText(String locatorKey, String text) {
        try {
            logger.info("Typing text in " + locatorKey + ": " + text);
            getLocatorType(locatorKey).sendKeys(text);
        } catch (Exception e) {
            logger.error("Failed to type text in " + locatorKey + ": " + e.getMessage());
            throw e;
        }
    }

    public static void clearAndTypeText(String locatorKey, String text) {
        try {
            WebElement element = getLocatorType(locatorKey);
            element.clear();
            Thread.sleep(300);
            element.sendKeys(text);
            logger.info("Cleared and typed text in " + locatorKey);
        } catch (Exception e) {
            logger.error("Failed to clear and type text in " + locatorKey + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void clickElement(String locatorkey) {
        try {
            logger.info("Clicking on element: " + locatorkey);
            getLocatorType(locatorkey).click();
        } catch (Exception e) {
            logger.error("Failed to click element " + locatorkey + ": " + e.getMessage());
            throw e;
        }
    }

    public static String getText(String locatorKey) {
        try {
            String text = getLocatorType(locatorKey).getText();
            logger.info("Retrieved text from " + locatorKey + ": " + text);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from " + locatorKey + ": " + e.getMessage());
            throw e;
        }
    }

    public static WebElement getLocatorType(String locatorKey) {
        if (!isElementPresent(locatorKey)) {
            logger.warn("Element not present: " + locatorKey);
        }
        try {
            return getDriver().findElement(getLocator(locatorKey));
        } catch (Exception e) {
            logger.error("Failed to find element " + locatorKey + ": " + e.getMessage());
            throw e;
        }
    }

    public static boolean isElementPresent(String locatorKey) {
        try {
            getWait().until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorKey)));
            logger.info("Element found: " + locatorKey);
            return true;
        } catch (Exception e) {
            logger.warn("Element not found: " + locatorKey);
            return false;
        }
    }

    public static By getLocator(String locatorKey) {
        if (locatorKey.endsWith("_id")) {
            return By.id(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_name")) {
            return By.name(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_className")) {
            return By.className(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_linkText")) {
            return By.linkText(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_xPath")) {
            return By.xpath(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_tagName")) {
            return By.tagName(orProperties.getProperty(locatorKey));
        } else if (locatorKey.endsWith("_partialLinkText")) {
            return By.partialLinkText(orProperties.getProperty(locatorKey));
        } else {
            return By.xpath(orProperties.getProperty(locatorKey));
        }
    }

    public static void selectFrame(String frameName) {
        logger.info("Switching to frame: " + frameName);
        getDriver().switchTo().frame(getDriver().findElement(By.name(frameName)));
    }

    public static void takeScreenshot(String testName) throws IOException {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateFormat = simpleDateFormat.format(date) + ".png";

        String screenshotDir = System.getProperty("user.dir") + "/failurescreenshots/";
        File dir = new File(screenshotDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create screenshot directory: " + screenshotDir);
            }
        }

        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        String screenshotPath = screenshotDir + testName + "_" + dateFormat;

        FileHandler.copy(scrFile, new File(screenshotPath));
        logger.info("Screenshot saved: " + screenshotPath);

        ExtentTest test = getExtentTest();
        if (test != null) {
            test.log(Status.INFO, "Screenshot --> " + test.addScreenCaptureFromPath(screenshotPath));
        }
    }

    public static void drawBorder(WebDriver webDriver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("arguments[0].style.border='5px solid yellow'", element);
        logger.info("Border drawn around element");
    }

    public static WebElement waitForElementClickable(String locatorKey) {
        By locator = getLocator(locatorKey);
        WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(locator));
        logger.info("Element is clickable: " + locatorKey);
        return element;
    }

    public static WebElement waitForElementVisible(String locatorKey) {
        By locator = getLocator(locatorKey);
        WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
        logger.info("Element is visible: " + locatorKey);
        return element;
    }

    public static String getAttributeValue(String locatorKey, String attributeName) {
        String value = getLocatorType(locatorKey).getAttribute(attributeName);
        logger.info("Attribute value of " + locatorKey + " (" + attributeName + "): " + value);
        return value;
    }

    public static boolean isElementEnabled(String locatorKey) {
        boolean isEnabled = getLocatorType(locatorKey).isEnabled();
        logger.info("Element enabled status " + locatorKey + ": " + isEnabled);
        return isEnabled;
    }

    public static boolean isElementDisplayed(String locatorKey) {
        try {
            boolean isDisplayed = getLocatorType(locatorKey).isDisplayed();
            logger.info("Element displayed status " + locatorKey + ": " + isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            logger.warn("Element not displayed: " + locatorKey);
            return false;
        }
    }

    public static void scrollToElement(String locatorKey) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        WebElement element = getLocatorType(locatorKey);
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        logger.info("Scrolled to element: " + locatorKey);
    }

    public static void pressKey(String locatorKey, Keys key) {
        getLocatorType(locatorKey).sendKeys(key);
        logger.info("Key pressed in " + locatorKey + ": " + key);
    }

    public static void keyboardShortcut(Keys... keys) {
        Actions action = new Actions(getDriver());
        for (Keys key : keys) {
            action.sendKeys(key);
        }
        action.perform();
        logger.info("Keyboard shortcut executed");
    }

    public static List<WebElement> getElements(String locatorKey) {
        List<WebElement> elements = getDriver().findElements(getLocator(locatorKey));
        logger.info("Found " + elements.size() + " elements for: " + locatorKey);
        return elements;
    }

    public static void acceptAlert() {
        getDriver().switchTo().alert().accept();
        logger.info("Alert accepted");
    }

    public static void dismissAlert() {
        getDriver().switchTo().alert().dismiss();
        logger.info("Alert dismissed");
    }

    public static String getAlertText() {
        String text = getDriver().switchTo().alert().getText();
        logger.info("Alert text: " + text);
        return text;
    }
}