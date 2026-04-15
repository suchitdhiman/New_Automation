package com.sk.webdrivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseClass {
	
	public static WebDriver webDriver;
	
	public static void browserLauncher(String browser) {
		if ( browser.equals("chrome")) {
			webDriver = new ChromeDriver();
		}else if(browser.equals("edge")) {
			webDriver = new EdgeDriver();
		}else if(browser.equals("firefox")) {
			webDriver = new FirefoxDriver();
		}
	}
	
	public static void navigate(String url) {
		webDriver.get(url);
		//navigate(url);
	}
	

}
