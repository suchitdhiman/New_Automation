package com.sk.automation.driver;

import com.sk.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Builds a configured {@link WebDriver} for a given browser.
 *
 * <p>Selenium 4 resolves and downloads the matching driver binary itself
 * (Selenium Manager), so there is no WebDriverManager dependency and no
 * {@code System.setProperty("webdriver.chrome.driver", ...)} anywhere.
 */
public final class DriverFactory {

    private static final Logger LOG = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Utility class — no instances.
    }

    public static WebDriver create(Browser browser) {
        LOG.info("Launching {} (headless={})", browser, isHeadless());

        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions());
            case EDGE -> new EdgeDriver(edgeOptions());
            case FIREFOX -> new FirefoxDriver(firefoxOptions());
        };

        applyTimeouts(driver);
        driver.manage().window().maximize();
        return driver;
    }

    private static void applyTimeouts(WebDriver driver) {
        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(ConfigManager.getInt("timeout.pageload")))
                .scriptTimeout(Duration.ofSeconds(ConfigManager.getInt("timeout.script")))
                // Implicit wait is pinned to zero on purpose. Mixing implicit and explicit
                // waits produces unpredictable, compounding timeouts — a classic source of
                // flaky suites. Every wait in this framework is explicit.
                .implicitlyWait(Duration.ZERO);
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        options.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
                "--remote-allow-origins=*", "--disable-notifications");
        options.setAcceptInsecureCerts(ConfigManager.getBoolean("browser.acceptInsecureCerts", false));
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        options.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        options.setAcceptInsecureCerts(ConfigManager.getBoolean("browser.acceptInsecureCerts", false));
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless()) {
            options.addArguments("-headless");
        }
        options.setAcceptInsecureCerts(ConfigManager.getBoolean("browser.acceptInsecureCerts", false));
        return options;
    }

    private static boolean isHeadless() {
        return ConfigManager.getBoolean("headless", false);
    }
}
