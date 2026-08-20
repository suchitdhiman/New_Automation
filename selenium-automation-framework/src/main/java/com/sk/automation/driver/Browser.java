package com.sk.automation.driver;

import com.sk.automation.exceptions.FrameworkException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Supported browsers.
 *
 * <p>An enum rather than free-text string comparison, so a typo such as "firfox"
 * fails immediately with a readable message instead of silently falling through
 * to an unhelpful branch.
 */
public enum Browser {

    CHROME,
    EDGE,
    FIREFOX;

    public static Browser from(String value) {
        if (value == null || value.isBlank()) {
            throw new FrameworkException("No browser specified. Supported values: " + supported());
        }
        return Arrays.stream(values())
                .filter(browser -> browser.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new FrameworkException(
                        "Unsupported browser '" + value + "'. Supported values: " + supported()));
    }

    private static String supported() {
        return Arrays.stream(values())
                .map(browser -> browser.name().toLowerCase())
                .collect(Collectors.joining(", "));
    }
}
