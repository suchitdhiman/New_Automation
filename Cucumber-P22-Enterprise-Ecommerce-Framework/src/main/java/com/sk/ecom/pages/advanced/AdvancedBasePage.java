package com.sk.ecom.pages.advanced;

import com.sk.ecom.config.ConfigManager;
import com.sk.ecom.enums.ConfigKey;
import com.sk.ecom.pages.base.BasePage;

/**
 * Shared root for the component-level UI pages.
 *
 * <p>These scenarios exercise the browser mechanics an enterprise application
 * relies on — native dialogs, frames, tabs, grids, uploads, async loading —
 * against a stable practice site, so a regression in the framework itself is
 * caught independently of the storefront being up.
 */
public abstract class AdvancedBasePage extends BasePage {

	/** @param path site-relative path, e.g. {@code /javascript_alerts} */
	protected void navigateTo(String path) {
		String base = ConfigManager.get(ConfigKey.APP_ADVANCED_URL);
		openUrl(base.endsWith("/") ? base.substring(0, base.length() - 1) + path : base + path);
	}
}
