package com.sk.webdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class WebDriverManager {

    public static WebDriver webDriver;
    public static Properties browsProperties;
    public static Properties urlProperties;
    public static Properties orProperties;
    public static WebDriverWait wait;
    public static Properties logProperties;
    public static ExtentReports extentReports;
    public static ExtentTest extentTest;

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
}