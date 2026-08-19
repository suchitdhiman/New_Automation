package com.sk.webDriver;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OperationHandler extends WebDriverManager {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		init();
		
		browserLauncher();
		
		urlSelect("amazon");
		
		Thread.sleep(2000);
		
		webDriver.navigate().refresh();
		
		
		WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

		// Open menu
		webDriver.findElement(By.id("searchDropdownBox")).sendKeys("Books"); 
		webDriver.findElement(By.id("twotabsearchtextbox")).sendKeys("Harry Potter");
		webDriver.findElement(By.id("nav-search-submit-button")).click();
		
	
		webDriver.quit();

		
		
		

	}

}
