package com.sk.ecom.pages.components;

import com.sk.ecom.pages.base.BasePage;
import com.sk.ecom.utils.BrowserUtils;

import org.openqa.selenium.By;

import java.util.Locale;

/**
 * Site footer, including the social links that open in a new tab.
 *
 * <p>{@link #openSocialLinkInNewTab(String)} returns the parent window handle so
 * the calling step can switch back deterministically. Leaving tab bookkeeping to
 * the step definitions is how suites end up asserting against the wrong window.
 */
public class FooterComponent extends BasePage {

	private static final By FOOTER = By.cssSelector("footer");
	private static final By COPY = By.cssSelector(".footer_copy");

	@Override
	public boolean isAt() {
		return isDisplayed(FOOTER);
	}

	public String copyright() {
		scrollTo(COPY);
		return textOf(COPY, "Footer copyright");
	}

	public boolean hasSocialLink(String network) {
		return isDisplayed(socialLink(network));
	}

	public String socialLinkTarget(String network) {
		return attributeOf(socialLink(network), "href", network + " link");
	}

	/**
	 * Clicks a social link and switches to the tab it opens.
	 *
	 * @return the handle of the original window, for switching back
	 */
	public String openSocialLinkInNewTab(String network) {
		String parent = BrowserUtils.currentWindow();
		click(socialLink(network), network + " link");
		BrowserUtils.switchToNewWindow(parent);
		return parent;
	}

	private By socialLink(String network) {
		return By.cssSelector("a[href*='" + network.toLowerCase(Locale.ROOT) + "']");
	}
}
