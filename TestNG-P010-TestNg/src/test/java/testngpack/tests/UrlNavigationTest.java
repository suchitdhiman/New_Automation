package testngpack.tests;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import testngpack.base.BaseClass;
import testngpack.utils.Log;

/**
 * Focused demo of {@code @Optional}.
 *
 * <p>Run it from a suite that declares "secondUrl" and that value is used; run
 * it from a suite that does not (or straight from the IDE) and the default in
 * the annotation is used instead. Nothing else changes.
 */
public class UrlNavigationTest extends BaseClass {

	@Test(description = "@Optional supplies a default when the xml omits the parameter")
	@Parameters({ "secondUrl" })
	public void navigateToSecondUrl(@Optional("bbc") String secondUrlKey) {
		Log.info("Second url key resolved to '" + secondUrlKey + "'");
		openUrl(secondUrlKey);
		Assert.assertNotNull(getTitle(), "Title should be available after navigation");
	}

	@Test(description = "Reading the same optional value programmatically from ITestContext")
	public void readParameterFromContext(ITestContext context) {
		String key = optionalParam(context, "secondUrl", "facebook");
		Log.info("optionalParam(secondUrl) -> " + key);
		openUrl(key);
		Assert.assertTrue(driver().getCurrentUrl().startsWith("http"), "Should have navigated somewhere");
	}
}
