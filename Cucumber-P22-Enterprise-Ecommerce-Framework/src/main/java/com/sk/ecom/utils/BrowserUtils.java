package com.sk.ecom.utils;

import com.sk.ecom.driver.DriverManager;
import com.sk.ecom.exceptions.FrameworkException;
import com.sk.ecom.logging.Log;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Everything that is about the <em>browser</em> rather than about a page:
 * windows and tabs, frames, native dialogs, cookies and navigation.
 *
 * <p>Separated from {@code BasePage} so these operations can also be called from
 * hooks and step definitions, where there is no page object in scope.
 */
public final class BrowserUtils {

	private BrowserUtils() {
		throw new IllegalStateException("Utility class");
	}

	private static WebDriver driver() {
		return DriverManager.getDriver();
	}

	/* ---------------------------------------------------------------- */
	/* Windows and tabs                                                   */
	/* ---------------------------------------------------------------- */

	public static String currentWindow() {
		return driver().getWindowHandle();
	}

	public static Set<String> allWindows() {
		return driver().getWindowHandles();
	}

	/** Waits for a second window to exist, then switches to it. */
	public static String switchToNewWindow(String parentHandle) {
		WaitFactory.wait().until(d -> d.getWindowHandles().size() > 1);
		String target = allWindows().stream()
				.filter(handle -> !handle.equals(parentHandle))
				.findFirst()
				.orElseThrow(() -> new FrameworkException("No new window appeared to switch to"));
		driver().switchTo().window(target);
		Log.step("Switched to newly opened window");
		return target;
	}

	public static void switchToWindowWithTitle(String title) {
		String parent = currentWindow();
		for (String handle : allWindows()) {
			driver().switchTo().window(handle);
			if (driver().getTitle() != null && driver().getTitle().contains(title)) {
				Log.step("Switched to window titled [%s]", driver().getTitle());
				return;
			}
		}
		driver().switchTo().window(parent);
		throw new FrameworkException("No open window has a title containing [" + title + "]");
	}

	public static void switchToWindowWithUrl(String urlFragment) {
		String parent = currentWindow();
		for (String handle : allWindows()) {
			driver().switchTo().window(handle);
			if (driver().getCurrentUrl().contains(urlFragment)) {
				return;
			}
		}
		driver().switchTo().window(parent);
		throw new FrameworkException("No open window has a URL containing [" + urlFragment + "]");
	}

	/** Closes every window except the given one and switches back to it. */
	public static void closeAllExcept(String keepHandle) {
		for (String handle : new LinkedHashSet<>(allWindows())) {
			if (!handle.equals(keepHandle)) {
				driver().switchTo().window(handle);
				driver().close();
			}
		}
		driver().switchTo().window(keepHandle);
	}

	public static void openNewTab(String url) {
		JavaScriptUtils.openNewTab(url);
	}

	/* ---------------------------------------------------------------- */
	/* Frames                                                             */
	/* ---------------------------------------------------------------- */

	public static void switchToFrame(By locator) {
		WaitFactory.wait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
		Log.step("Switched into frame located by [%s]", locator);
	}

	public static void switchToFrame(int index) {
		WaitFactory.wait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
		Log.step("Switched into frame at index [%d]", index);
	}

	public static void switchToFrame(String nameOrId) {
		WaitFactory.wait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(nameOrId));
		Log.step("Switched into frame [%s]", nameOrId);
	}

	public static void switchToDefaultContent() {
		driver().switchTo().defaultContent();
		Log.step("Switched back to the main document");
	}

	public static void switchToParentFrame() {
		driver().switchTo().parentFrame();
	}

	public static int frameCount() {
		return driver().findElements(By.tagName("iframe")).size();
	}

	/* ---------------------------------------------------------------- */
	/* Native dialogs                                                     */
	/* ---------------------------------------------------------------- */

	public static Alert awaitAlert() {
		return WaitFactory.wait().until(ExpectedConditions.alertIsPresent());
	}

	public static boolean isAlertPresent() {
		try {
			driver().switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}

	public static String acceptAlert() {
		Alert alert = awaitAlert();
		String text = alert.getText();
		alert.accept();
		Log.step("Accepted alert: %s", text);
		return text;
	}

	public static String dismissAlert() {
		Alert alert = awaitAlert();
		String text = alert.getText();
		alert.dismiss();
		Log.step("Dismissed alert: %s", text);
		return text;
	}

	public static String alertText() {
		return awaitAlert().getText();
	}

	public static String typeInAlertAndAccept(String text) {
		Alert alert = awaitAlert();
		String prompt = alert.getText();
		alert.sendKeys(text);
		alert.accept();
		Log.step("Typed [%s] into prompt and accepted", text);
		return prompt;
	}

	/* ---------------------------------------------------------------- */
	/* Cookies and navigation                                             */
	/* ---------------------------------------------------------------- */

	public static void addCookie(String name, String value) {
		driver().manage().addCookie(new Cookie(name, value));
	}

	public static Cookie cookie(String name) {
		return driver().manage().getCookieNamed(name);
	}

	public static List<String> cookieNames() {
		return driver().manage().getCookies().stream().map(Cookie::getName).collect(Collectors.toList());
	}

	public static void deleteAllCookies() {
		driver().manage().deleteAllCookies();
		Log.step("Cleared all cookies");
	}

	public static void navigateBack() {
		driver().navigate().back();
		WaitFactory.waitForPageLoad();
	}

	public static void navigateForward() {
		driver().navigate().forward();
		WaitFactory.waitForPageLoad();
	}

	public static void refresh() {
		driver().navigate().refresh();
		WaitFactory.waitForPageLoad();
	}

	/** Scrolls an element into view and returns it; handy from step definitions. */
	public static WebElement reveal(By locator) {
		WebElement element = WaitFactory.waitFor(locator, com.sk.ecom.enums.WaitStrategy.PRESENCE);
		JavaScriptUtils.scrollIntoView(element);
		return element;
	}
}
