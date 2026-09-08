package com.sk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.sk.core.ConfigManager;
import com.sk.core.DriverFactory;

/**
 * Failure screenshots.
 *
 * <p>Everything here returns an {@link Optional} and swallows its own errors on
 * purpose. A screenshot is diagnostic material; if the browser has already
 * crashed, failing to photograph the crash must not replace the real failure
 * with an {@code UnreachableBrowserException} in the report.
 */
public final class ScreenshotUtil {

	private static final Logger LOG = LogManager.getLogger(ScreenshotUtil.class);
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

	private ScreenshotUtil() {
		// static utility
	}

	/**
	 * @param label usually {@code ClassName.methodName}; it becomes part of the file name.
	 * @return where the PNG landed, or empty when there was nothing to capture.
	 */
	public static Optional<Path> capture(String label) {
		if (!ConfigManager.getBoolean("screenshot.on.failure")) {
			return Optional.empty();
		}
		if (!DriverFactory.hasDriver()) {
			// Pure TestNG concept tests have no browser. Not a problem, not a warning.
			return Optional.empty();
		}

		try {
			WebDriver driver = DriverFactory.getDriver();
			byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

			Path directory = Paths.get(ConfigManager.get("screenshot.dir"));
			Files.createDirectories(directory);

			Path target = directory.resolve(sanitise(label) + "_" + LocalDateTime.now().format(STAMP) + ".png");
			Files.write(target, png);

			LOG.info("Screenshot saved: {}", target.toAbsolutePath());
			return Optional.of(target);
			// WebDriverException is a RuntimeException, so one catch covers a dead
			// browser, a stale session and anything else the driver throws.
		} catch (IOException | RuntimeException e) {
			LOG.warn("Could not capture a screenshot for [{}]: {}", label, e.getMessage());
			return Optional.empty();
		}
	}

	/** Test names contain brackets, spaces and slashes from data providers; file systems do not like those. */
	private static String sanitise(String label) {
		String cleaned = label.replaceAll("[^A-Za-z0-9._-]", "_");
		return cleaned.length() > 90 ? cleaned.substring(0, 90) : cleaned;
	}
}
