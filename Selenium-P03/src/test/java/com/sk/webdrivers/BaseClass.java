package com.sk.webdrivers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseClass {
	
	public static WebDriver webDriver;
	public static File f;
	public static FileInputStream fis;
	public static Properties p;
	public static Properties mainProperties;
	public static Properties childProperties;
	
	public static void init() throws Exception {
		System.out.println("BaseClass.init()");
		
		//base common path
		String projectRootPath = System.getProperty("user.dir");
		System.out.println(projectRootPath);
		//test path
		String testResourcePath = "\\src\\test\\resources";
		System.out.println(testResourcePath);
		//load the data file
		f = new File(projectRootPath+testResourcePath+"\\data.properties");
		fis = new FileInputStream(f);
		p = new Properties();
		p.load(fis);
		
		//Load the environment file
		f = new File(projectRootPath+testResourcePath+"\\env.properties");
		fis = new FileInputStream(f);
		mainProperties = new Properties();
		mainProperties.load(fis);
		String e = mainProperties.getProperty("environment");
		System.out.println(e);
		
		//load the prod file
		f = new File(projectRootPath+testResourcePath+"\\"+e+".properties");
		fis= new FileInputStream(f);
		childProperties = new Properties();
		childProperties.load(fis);
		String value = childProperties.getProperty("amazonurl");
		System.out.println(value);
		
		
		
	}
	
	public static void browserLauncher(String browser) {
		String browserurl = p.getProperty(browser);
		if (browser.equals("chrome")) {
			webDriver = new ChromeDriver();
		}else if(browserurl.equals("edge")) {
			webDriver = new EdgeDriver();
		}else if(browserurl.equals("firefox")) {
			webDriver = new FirefoxDriver();
		}else {
			System.out.println("please enter correct one");
		}
	}
	
	public static void navigate(String urlKey) {
	    String actualUrl = childProperties.getProperty(urlKey);
	    
	    if (actualUrl != null) {
	        webDriver.get(actualUrl);
	    } else {
	        System.out.println("URL not found for key: " + urlKey);
	    }
	}
	

}
