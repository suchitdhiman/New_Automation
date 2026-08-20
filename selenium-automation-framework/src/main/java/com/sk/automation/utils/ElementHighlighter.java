package com.sk.automation.utils;

import com.sk.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Draws a temporary outline around an element before a screenshot, so the person
 * reading the report can see exactly which control was in play.
 *
 * <p>Gated behind {@code ui.highlight.enabled} and off by default: repainting an
 * element on every interaction costs real time across a large suite, and it is
 * only useful when a human is watching.
 */
public final class ElementHighlighter {

    private static final Logger LOG = LogManager.getLogger(ElementHighlighter.class);
    private static final String STYLE = "outline: 3px solid #FFD200; outline-offset: 2px;";

    private ElementHighlighter() {
        // Utility class — no instances.
    }

    public static void highlight(WebDriver driver, WebElement element) {
        if (!ConfigManager.getBoolean("ui.highlight.enabled", false)) {
            return;
        }
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].setAttribute('style', arguments[1] + arguments[0].getAttribute('style'));",
                    element, STYLE);
        } catch (Exception e) {
            // Cosmetic only — never let it break a test.
            LOG.debug("Highlight skipped: {}", e.getMessage());
        }
    }
}
