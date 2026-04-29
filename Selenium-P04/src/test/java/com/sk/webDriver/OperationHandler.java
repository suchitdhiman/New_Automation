package com.sk.webDriver;

import java.io.IOException;
import java.util.Set;

import org.openqa.selenium.WindowType;

public class OperationHandler extends WebDriverManager {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		init();
		
		browserLauncher();
		
		urlSelect("facebook");
		
		Thread.sleep(5000);
		
		String tittle = webDriver.getTitle();
		System.out.println(tittle);
		
		String url = webDriver.getCurrentUrl();
		System.out.println(url);
		
		String page = webDriver.getPageSource();
		//System.out.println(page);
		
		String handler = webDriver.getWindowHandle();
		System.out.println(handler);
		
		webDriver.switchTo().newWindow(WindowType.WINDOW);
		urlSelect("amazon");
		
		
		
		Set<String> h1d2 = webDriver.getWindowHandles();
		
		for(String h:h1d2) {
			System.out.println(h);
		}
		

	}

}
