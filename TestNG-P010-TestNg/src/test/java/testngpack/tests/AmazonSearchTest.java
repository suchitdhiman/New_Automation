package testngpack.tests;

import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import testngpack.base.BaseClass;
import testngpack.base.DriverFactory;
import testngpack.utils.Log;

/**
 * @Parameters / @Optional on the test method itself.
 *
 * <p>The suite xml supplies "browser" (consumed by BaseClass#setUp) and may
 * supply "category"/"searchTerm". When it does not, the @Optional defaults are
 * used, so this class also runs green from Eclipse without any xml.
 */
public class AmazonSearchTest extends BaseClass {

	@Test(description = "Search a product on Amazon using xml-driven parameters")
	@Parameters({ "category", "searchTerm" })
	public void searchProduct(@Optional("Books") String category,
			@Optional("Harry Potter") String searchTerm) {

		Log.info("Searching '" + searchTerm + "' under '" + category + "'");

		selectOption("amazondropbox_id", category);
		textType("amazonsearchtextbox_name", searchTerm);
		clickElement("amazonsearchbutton_xPath");

		String title = getTitle();
		Assert.assertTrue(title.toLowerCase().contains(searchTerm.split(" ")[0].toLowerCase()),
				"Result page title should mention the search term but was: " + title);
	}

	@Test(description = "Home page loads on the browser passed from the suite xml")
	public void homePageLoads() {
		Assert.assertTrue(getTitle() != null && !getTitle().isEmpty(),
				"Home page title should not be empty on " + DriverFactory.getBrowserName());
	}
}
