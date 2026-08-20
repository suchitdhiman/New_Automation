package com.sk.frames;

import java.io.IOException;

import org.openqa.selenium.By;

public class SwitchFrames extends com.sk.frames.BaseClass {

	public static void main(String[] args)  {
		
		try {
			init();
			browserLauncher();
			selectUrl("java");
			selectFrame("packageListFrame");
			webDriver.findElement(By.linkText("java.applet")).click();
			webDriver.switchTo().defaultContent();
			
			Thread.sleep(2000);
			
			selectFrame("packageFrame");
			webDriver.findElement(By.linkText("Applet")).click();
			webDriver.switchTo().defaultContent();
			Thread.sleep(2000);
			
			selectFrame("classFrame");
			webDriver.findElement(By.linkText("JApplet")).click();
			Thread.sleep(2000);

		}catch (IOException io) {
			System.out.println("IO Exception:: ");
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			webDriver.quit();
			extentReports.flush();
		}
	}

}
