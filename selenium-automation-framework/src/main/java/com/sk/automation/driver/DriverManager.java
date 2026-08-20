package com.sk.automation.driver;

import com.sk.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Holds the {@link WebDriver} for the current thread.
 *
 * <p>This is the single change that makes parallel execution possible. With one
 * {@code static WebDriver} shared across the suite, two tests running at once
 * fight over the same browser window and results become meaningless. A
 * {@link ThreadLocal} gives each test thread its own isolated driver.
 *
 * <p>{@link #quit()} always calls {@code remove()} — leaving entries behind in a
 * pooled thread leaks browser processes over a long run.
 */
public final class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
        // Utility class — no instances.
    }

    public static void set(WebDriver driver) {
        if (driver == null) {
            throw new FrameworkException("Refusing to store a null WebDriver");
        }
        DRIVER.set(driver);
    }

    public static WebDriver get() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException(
                    "No WebDriver bound to thread '" + Thread.currentThread().getName()
                            + "'. Does this test extend BaseTest?");
        }
        return driver;
    }

    public static boolean isInitialised() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (Exception e) {
            // A browser that has already crashed must not mask the real test failure.
            LOG.warn("Ignoring error while quitting the driver: {}", e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }
}
