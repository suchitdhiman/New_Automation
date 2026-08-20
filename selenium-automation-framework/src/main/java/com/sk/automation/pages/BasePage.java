package com.sk.automation.pages;

import com.sk.automation.config.ConfigManager;
import com.sk.automation.driver.DriverManager;
import com.sk.automation.utils.ElementHighlighter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared behaviour for every page object: waits, logging and the small set of
 * interactions pages actually need.
 *
 * <p>Each method waits for the right condition before acting — visible to read,
 * clickable to click — rather than hoping the element is ready. There is no
 * {@code Thread.sleep} anywhere in this framework; fixed sleeps are simultaneously
 * too slow on a fast machine and too short on a loaded CI agent.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverManager.get();
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigManager.getInt("timeout.explicit")));
    }

    protected void open(String url) {
        log.info("Navigating to {}", url);
        driver.get(url);
    }

    protected WebElement waitForVisible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ElementHighlighter.highlight(driver, element);
        return element;
    }

    protected WebElement waitForClickable(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ElementHighlighter.highlight(driver, element);
        return element;
    }

    protected void click(By locator) {
        log.debug("Clicking {}", locator);
        waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        log.debug("Typing into {}", locator);
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String textOf(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitForVisible(locator).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Waits for a frame to exist and switches into it.
     *
     * <p>{@code frameToBeAvailableAndSwitchToIt} is what replaces the
     * {@code Thread.sleep(2000)} that usually surrounds frame handling: it polls
     * until the frame is genuinely attached rather than guessing at a duration.
     */
    protected void switchToFrame(String nameOrId) {
        log.debug("Switching to frame '{}'", nameOrId);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(nameOrId));
    }

    protected void switchToFrame(By locator) {
        log.debug("Switching to frame located by {}", locator);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    protected void switchToDefaultContent() {
        log.debug("Switching back to the default content");
        driver.switchTo().defaultContent();
    }

    public String pageTitle() {
        return driver.getTitle();
    }
}
