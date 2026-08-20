package com.sk.automation.utils;

import com.sk.automation.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

/**
 * Captures a failure screenshot once and serves it in both forms that matter.
 *
 * <p>A PNG under {@code target/screenshots} is what CI archives. The Base64 copy is
 * embedded directly into the HTML report, because a report that only links to files
 * on disk shows broken images the moment someone emails it or downloads it from a
 * build server.
 *
 * <p>The browser is queried exactly once per failure; encoding is done in memory.
 * Paths are built with {@link Path} rather than string concatenation, so nothing here
 * is tied to Windows separators.
 */
public final class ScreenshotUtil {

    private static final Logger LOG = LogManager.getLogger(ScreenshotUtil.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Path SCREENSHOT_DIR = Path.of(System.getProperty("user.dir"), "target", "screenshots");

    private ScreenshotUtil() {
        // Utility class — no instances.
    }

    /** A captured screenshot: where it was written, and its Base64 encoding. */
    public record Screenshot(Optional<Path> file, String base64) {
    }

    /**
     * Captures the current viewport. Returns empty when the browser has already gone —
     * a driver that crashed must not replace the real assertion failure with an
     * exception raised while trying to photograph it.
     */
    public static Optional<Screenshot> capture(String testName) {
        if (!DriverManager.isInitialised()) {
            LOG.debug("No driver on this thread — skipping screenshot");
            return Optional.empty();
        }

        try {
            byte[] image = ((TakesScreenshot) DriverManager.get()).getScreenshotAs(OutputType.BYTES);
            return Optional.of(new Screenshot(writeToDisk(image, testName),
                    Base64.getEncoder().encodeToString(image)));
        } catch (Exception e) {
            LOG.warn("Could not capture screenshot for '{}': {}", testName, e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<Path> writeToDisk(byte[] image, String testName) {
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Path target = SCREENSHOT_DIR.resolve(
                    sanitise(testName) + "_" + LocalDateTime.now().format(TIMESTAMP) + ".png");
            Files.write(target, image);
            LOG.info("Screenshot saved: {}", target);
            return Optional.of(target);
        } catch (Exception e) {
            LOG.warn("Screenshot captured but could not be written to disk: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String sanitise(String testName) {
        return (testName == null || testName.isBlank())
                ? "unknown"
                : testName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
