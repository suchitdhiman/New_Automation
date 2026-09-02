package testngpack.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import testngpack.base.BaseClass;
import testngpack.dataproviders.SearchDataProvider;
import testngpack.utils.Log;

/**
 * Target for data-provider level parallelism.
 *
 * <p>One method, three rows, three threads - controlled by
 * {@code data-provider-thread-count} on the suite.
 */
public class DataProviderParallelTest extends BaseClass {

	@Test(dataProvider = "searchTermsParallel", dataProviderClass = SearchDataProvider.class)
	public void searchEachTerm(String category, String term) {
		Log.info("Row [" + category + " / " + term + "] on thread " + Thread.currentThread().getName());

		selectOption("amazondropbox_id", category);
		textType("amazonsearchtextbox_name", term);
		clickElement("amazonsearchbutton_xPath");

		// Wait for the navigation instead of asserting on the old url.
		waitForUrlContaining("k=");
		Assert.assertTrue(driver().getCurrentUrl().contains("k="),
				"Search results url should carry the query string");
	}

	@Test(dataProvider = "urlKeysParallel", dataProviderClass = SearchDataProvider.class)
	public void openEachUrl(String urlKey) {
		openUrl(urlKey);
		Assert.assertNotNull(getTitle(), "Title should load for " + urlKey);
	}
}
