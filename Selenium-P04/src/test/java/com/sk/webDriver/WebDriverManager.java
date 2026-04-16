package com.sk.webDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class WebDriverManager {
	
	public static WebDriver webDriver;
	public static File file;
	public static FileInputStream fileInputStream;
	public static Properties browserPro;
	public static Properties urlPro;
	
	
	
	public static void init() throws IOException {
		
		String defaultPath = System.getProperty("user.dir");
		
		//load the data
		file = new File(defaultPath+"\\src\\test\\resources\\browser.properties");
		fileInputStream = new FileInputStream(file);
		browserPro = new Properties();
		browserPro.load(fileInputStream);
		
		
		//load the url file
		file = new File(defaultPath+"\\src\\test\\resources\\url.properties");
		fileInputStream = new FileInputStream(file);
		urlPro = new Properties();
		urlPro.load(fileInputStream);
	
	}
	
	public static void browserLauncher(){
		
		String brows = browserPro.getProperty("browser");
		if (brows.equalsIgnoreCase("chrome")){
		webDriver = new ChromeDriver();
		}else if(brows.equalsIgnoreCase("edge")) {
		webDriver = new EdgeDriver();
		}else {
			System.out.println("Not Valid");
		}
		webDriver.manage().window().maximize();
	}
	
	public static void urlSelect(String site) {
		String url = urlPro.getProperty(site);
		webDriver.get(url);
	}
}
