package com.sk.webdrivers;

public class TC01 extends BaseClass {

	public static void main(String[] args) {
		
		browserLauncher("chrome");
		navigate("https://www.amazon.in/");
		
		browserLauncher("edge");
		navigate("https://www.amazon.in/");
		

	}

}
