package com.sk.ecom.utils;

import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.driver.DriverManager;
import com.sk.ecom.logging.Log;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot capture that never throws.
 *
 * <p>A failing screenshot must not mask the assertion that caused it. Every
 * method here degrades to a log line and a {@code null}/empty result rather than
 * replacing the real failure with a {@code WebDriverException}.
 */
public final class ScreenshotUtils {

	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

	private ScreenshotUtils() {
		throw new IllegalStateException("Utility class");
	}

	/** @return base64 PNG for embedding in the report, or {@code null}. */
	public static String captureBase64() {
		if (!DriverManager.hasDriver()) {
			return null;
		}
		try {
			return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
		} catch (Exception e) {
			Log.warn("Could not capture screenshot: " + e.getMessage());
			return null;
		}
	}

	/** @return raw PNG bytes for {@code scenario.attach(...)}, or {@code null}. */
	public static byte[] captureBytes() {
		if (!DriverManager.hasDriver()) {
			return null;
		}
		try {
			return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
		} catch (Exception e) {
			Log.warn("Could not capture screenshot: " + e.getMessage());
			return null;
		}
	}

	/** Writes a PNG to {@code target/screenshots}; @return the path, or {@code null}. */
	public static Path captureToFile(String name) {
		byte[] image = captureBytes();
		if (image == null) {
			return null;
		}
		try {
			Path dir = Paths.get(FrameworkConstants.SCREENSHOT_DIR);
			Files.createDirectories(dir);
			Path file = dir.resolve(sanitise(name) + "_" + LocalDateTime.now().format(STAMP) + ".png");
			Files.write(file, image);
			Log.debug("Screenshot saved: " + file);
			return file;
		} catch (IOException e) {
			Log.warn("Could not write screenshot to disk: " + e.getMessage());
			return null;
		}
	}

	/** Crops to a single element — much easier to read in a report than a full page. */
	public static String captureElementBase64(WebElement element) {
		try {
			return element.getScreenshotAs(OutputType.BASE64);
		} catch (Exception e) {
			Log.warn("Could not capture element screenshot: " + e.getMessage());
			return null;
		}
	}

	private static String sanitise(String name) {
		return name == null ? "screenshot" : name.replaceAll("[^A-Za-z0-9._-]", "_");
	}
}
