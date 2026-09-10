package com.sk.ecom.driver;

import com.sk.ecom.logging.Log;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Cross-cutting driver logging, attached via Selenium 4's
 * {@code EventFiringDecorator}.
 *
 * <p>The point is diagnosis of a failure long after the run: every navigation
 * and every driver-level error lands in the log file with the scenario name
 * already in the pattern, so a red build can be read without reproducing it.
 *
 * <p>Intentionally does <em>not</em> call {@code toString()} on elements — that
 * costs an extra round trip to the browser on every single interaction, which
 * on a large suite is minutes of wall clock for no benefit.
 */
public class WebDriverLogListener implements WebDriverListener {

	@Override
	public void beforeGet(WebDriver driver, String url) {
		Log.debug("Navigating to: " + url);
	}

	@Override
	public void afterGet(WebDriver driver, String url) {
		Log.debug("Loaded: " + url);
	}

	@Override
	public void beforeQuit(WebDriver driver) {
		Log.debug("Quitting browser session");
	}

	@Override
	public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {
		Throwable cause = e.getCause() == null ? e : e.getCause();
		Log.error("WebDriver call [" + method.getName() + "] failed: " + cause.getMessage());
	}
}
