package com.sk.automation.tests;

import com.sk.automation.base.BaseTest;
import com.sk.automation.pages.JavaApiDocsPage;
import com.sk.automation.pages.NestedFramesPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Frame-switching coverage.
 *
 * <p>Every test states an expected outcome. The original script navigated frames and
 * printed to the console, which means it could only fail on an exception — a page
 * that loaded entirely the wrong content would have been reported as a pass.
 */
public class SwitchFramesTest extends BaseTest {

    @Test(groups = {"regression", "frames"},
            description = "Selecting a package then a type renders that type in the class frame")
    public void shouldDisplaySelectedTypeInClassFrame() {
        String heading = new JavaApiDocsPage()
                .open()
                .selectPackage("java.applet")
                .selectType("Applet")
                .displayedTypeHeading();

        assertThat(heading)
                .as("Class frame should show the selected type")
                .containsIgnoringCase("Applet");
    }

    @Test(groups = {"regression", "frames"},
            description = "The package list frame stays reachable after working inside the class frame")
    public void shouldReturnToPackageListAfterEnteringClassFrame() {
        JavaApiDocsPage page = new JavaApiDocsPage()
                .open()
                .selectPackage("java.applet")
                .selectType("Applet");

        assertThat(page.displayedTypeHeading()).containsIgnoringCase("Applet");

        // Navigating back to a sibling frame is the step that fails when code forgets
        // to return to the default content first.
        String heading = page.selectPackage("java.awt")
                .selectType("Button")
                .displayedTypeHeading();

        assertThat(heading)
                .as("A second package should be selectable without reloading the page")
                .containsIgnoringCase("Button");
    }

    @Test(groups = {"smoke", "frames"},
            description = "Text is readable from a frame nested inside another frame")
    public void shouldReadTextFromNestedFrame() {
        String text = new NestedFramesPage()
                .open()
                .textInNestedFrame("frame-middle");

        assertThat(text)
                .as("Middle frame should expose its content once both frames are entered")
                .isNotBlank();
    }

    @Test(groups = {"smoke", "frames"},
            description = "Sibling frames are reachable after returning to the default content")
    public void shouldReachSiblingFrameAfterReturningToDefaultContent() {
        NestedFramesPage page = new NestedFramesPage().open();

        String nested = page.textInNestedFrame("frame-middle");
        String bottom = page.textInBottomFrame();

        assertThat(nested).isNotBlank();
        assertThat(bottom)
                .as("Bottom frame is a sibling — reachable only via the default content")
                .isNotBlank();
    }
}
