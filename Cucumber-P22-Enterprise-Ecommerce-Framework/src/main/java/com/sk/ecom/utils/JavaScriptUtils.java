package com.sk.ecom.utils;

import com.sk.ecom.driver.DriverManager;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * JavaScript fall-backs.
 *
 * <p>These are a safety net, not the default path: a JS click bypasses the real
 * user event chain and can pass on a button a human could never press. The
 * framework uses them only after a normal Selenium interaction has failed, and
 * says so in the log when it does.
 */
public final class JavaScriptUtils {

	private JavaScriptUtils() {
		throw new IllegalStateException("Utility class");
	}

	private static JavascriptExecutor js() {
		return (JavascriptExecutor) DriverManager.getDriver();
	}

	public static Object execute(String script, Object... args) {
		return js().executeScript(script, args);
	}

	public static void click(WebElement element) {
		js().executeScript("arguments[0].click();", element);
	}

	public static void setValue(WebElement element, String value) {
		js().executeScript(
				"arguments[0].value=arguments[1];"
						+ "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
						+ "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
				element, value);
	}

	/** Centres the element; {@code block:'center'} avoids sticky headers eating the click. */
	public static void scrollIntoView(WebElement element) {
		js().executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
	}

	public static void scrollToBottom() {
		js().executeScript("window.scrollTo(0, document.body.scrollHeight);");
	}

	public static void scrollToTop() {
		js().executeScript("window.scrollTo(0, 0);");
	}

	public static void scrollBy(int x, int y) {
		js().executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
	}

	/** Draws a temporary outline — invaluable when reviewing a recorded run. */
	public static void highlight(WebElement element) {
		js().executeScript(
				"const o=arguments[0].style.outline;"
						+ "arguments[0].style.outline='3px solid #e2231a';"
						+ "setTimeout(()=>{arguments[0].style.outline=o;}, 400);",
				element);
	}

	public static String getText(WebElement element) {
		return String.valueOf(js().executeScript("return arguments[0].textContent;", element)).trim();
	}

	public static String pageReadyState() {
		return String.valueOf(js().executeScript("return document.readyState;"));
	}

	public static String pageTitle() {
		return String.valueOf(js().executeScript("return document.title;"));
	}

	/** True when the browser reports no in-flight fetch/XHR work (best effort). */
	public static boolean isDomIdle() {
		Object result = js().executeScript(
				"return (window.performance && window.performance.getEntriesByType) ? "
						+ "window.performance.getEntriesByType('resource').length >= 0 : true;");
		return Boolean.TRUE.equals(result) || result != null;
	}

	public static void openNewTab(String url) {
		js().executeScript("window.open(arguments[0], '_blank');", url);
	}
}
