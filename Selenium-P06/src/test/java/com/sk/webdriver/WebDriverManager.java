package com.sk.webdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Browser;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebDriverManager {
	
	public static WebDriver webDriver;
	public static Properties browsProperties;
	public static File file;
	public static FileInputStream fileInputStream;
	public static Properties urlProperties;
	public static Properties orProperties;
	public static WebDriverWait wait;
	
	
	
	//intial method to initiate webdriver method
	public static void intit() throws IOException {
		
		//Load the url file
		String defaultPath = System.getProperty("user.dir");
		file = new File(defaultPath+"\\src\\test\\resources\\browser.properties");
		fileInputStream = new FileInputStream(file);
		browsProperties = new Properties();
		browsProperties.load(fileInputStream);
		
		//load the url file
		file = new File(defaultPath+"\\src\\test\\resources\\url.properties");
		fileInputStream = new FileInputStream(file);
		urlProperties = new Properties();
		urlProperties.load(fileInputStream);
		
		//load the or file
		file = new File(defaultPath+"\\src\\test\\resources\\or.properties");
		fileInputStream = new FileInputStream(file);
		orProperties = new Properties();
		orProperties.load(fileInputStream);
		
	}
	
	public static void browserLaunch(String browser) {
		
		if(browser.equals("chrome")) {
			webDriver = new ChromeDriver();
		} else if(browser.equals("edge")) {
			webDriver = new EdgeDriver();
		} else if(browser.equals("firefox")) {
			webDriver = new FirefoxDriver();
		}else {
			System.out.println("Not valid input!");
		}
		webDriver.manage().window().maximize();
		wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
		
		
	}
	
	public static void selectUrl(String site) {
		//String url = urlProperties.getProperty(site);
		
		webDriver.get(urlProperties.getProperty(site));
	}

}
