package com.sk.automation.pages;

import com.sk.automation.config.ConfigManager;
import org.openqa.selenium.By;

/**
 * Page object for the framed Java API documentation site.
 *
 * <p>The layout is three sibling frames — a package list, a package detail list and
 * the class detail pane. Callers say what they want ({@code selectPackage("java.applet")})
 * and this class decides which frame that lives in. No test should have to know a
 * frame name.
 *
 * <p>Each method returns {@code this} so a scenario reads as one sentence.
 */
public class JavaApiDocsPage extends BasePage {

    private static final String PACKAGE_LIST_FRAME = "packageListFrame";
    private static final String PACKAGE_FRAME = "packageFrame";
    private static final String CLASS_FRAME = "classFrame";

    private static final By CLASS_HEADING = By.cssSelector("h2, h1");

    public JavaApiDocsPage open() {
        open(ConfigManager.get("url.javadocs"));
        return this;
    }

    /** Picks a package from the left-hand package list, e.g. {@code java.applet}. */
    public JavaApiDocsPage selectPackage(String packageName) {
        log.info("Selecting package '{}'", packageName);
        switchToDefaultContent();
        switchToFrame(PACKAGE_LIST_FRAME);
        click(By.linkText(packageName));
        switchToDefaultContent();
        return this;
    }

    /** Picks a type from the package detail frame, e.g. {@code Applet}. */
    public JavaApiDocsPage selectType(String typeName) {
        log.info("Selecting type '{}'", typeName);
        switchToDefaultContent();
        switchToFrame(PACKAGE_FRAME);
        click(By.linkText(typeName));
        switchToDefaultContent();
        return this;
    }

    /** Heading currently rendered in the class detail pane — the assertable outcome. */
    public String displayedTypeHeading() {
        switchToDefaultContent();
        switchToFrame(CLASS_FRAME);
        String heading = textOf(CLASS_HEADING);
        switchToDefaultContent();
        return heading;
    }
}
