package com.sk.automation.pages;

import com.sk.automation.config.ConfigManager;
import org.openqa.selenium.By;

/**
 * Page object for a nested-frame demo page (frame inside a frame).
 *
 * <p>Kept as a stable second target: public documentation sites change their markup
 * without notice, and a suite whose only frame coverage depends on a third party
 * goes red for reasons that have nothing to do with the code under test.
 */
public class NestedFramesPage extends BasePage {

    private static final String TOP_FRAME = "frame-top";
    private static final String BOTTOM_FRAME = "frame-bottom";
    private static final By BODY = By.tagName("body");

    public NestedFramesPage open() {
        open(ConfigManager.get("url.nestedFrames"));
        return this;
    }

    /**
     * Reads text from a frame nested inside the top frame.
     *
     * <p>Nesting is the part people get wrong: switching to a child frame is relative
     * to the current frame, so the driver must be reset to the default content before
     * addressing a different branch of the tree.
     */
    public String textInNestedFrame(String childFrameName) {
        switchToDefaultContent();
        switchToFrame(TOP_FRAME);
        switchToFrame(childFrameName);
        String text = textOf(BODY);
        switchToDefaultContent();
        return text;
    }

    public String textInBottomFrame() {
        switchToDefaultContent();
        switchToFrame(BOTTOM_FRAME);
        String text = textOf(BODY);
        switchToDefaultContent();
        return text;
    }
}
