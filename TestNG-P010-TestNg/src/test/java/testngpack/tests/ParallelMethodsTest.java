package testngpack.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import testngpack.base.BaseClass;
import testngpack.base.DriverFactory;
import testngpack.utils.Log;

/**
 * Target for {@code parallel="methods"}.
 *
 * <p>Each method gets its own thread, therefore its own @BeforeMethod, therefore
 * its own browser. The thread name printed in the log makes that visible.
 */
public class ParallelMethodsTest extends BaseClass {

	@Test
	public void searchBox_isDisplayed() {
		Assert.assertTrue(isElementPresent("amazonsearchtextbox_name"), "Search box should be present");
		trace("searchBox_isDisplayed");
	}

	@Test
	public void categoryDropdown_isDisplayed() {
		Assert.assertTrue(isElementPresent("amazondropbox_id"), "Category dropdown should be present");
		trace("categoryDropdown_isDisplayed");
	}

	@Test
	public void searchButton_isDisplayed() {
		Assert.assertTrue(isElementPresent("amazonsearchbutton_xPath"), "Search button should be present");
		trace("searchButton_isDisplayed");
	}

	@Test
	public void pageTitle_isNotBlank() {
		Assert.assertFalse(getTitle().trim().isEmpty(), "Title should not be blank");
		trace("pageTitle_isNotBlank");
	}

	private void trace(String method) {
		Log.info(method + " ran on thread " + Thread.currentThread().getName()
				+ " with a " + DriverFactory.getBrowserName() + " driver");
	}
}
