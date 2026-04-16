package com.sk.webDriver;

import java.io.IOException;

public class OperationHandler extends WebDriverManager {

	public static void main(String[] args) throws IOException {
		
		init();
		
		browserLauncher();
		
		urlSelect("facebook");
		

	}

}
