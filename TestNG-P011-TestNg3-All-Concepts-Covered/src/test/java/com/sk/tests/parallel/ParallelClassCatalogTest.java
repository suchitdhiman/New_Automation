package com.sk.tests.parallel;

import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 3b - {@code parallel="classes"}, part 2 of 3. See
 * {@link ParallelClassLoginTest} for the explanation.
 */
public class ParallelClassCatalogTest extends BaseWebTest {

	@Test(groups = { Groups.SMOKE, Groups.CATALOG },
			description = "The catalogue lists exactly six products")
	public void catalogueListsSixProducts() {
		ThreadAuditor.record(ParallelClassLoginTest.SCOPE, "CatalogTest.catalogueListsSixProducts");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertEquals(products.productCount(), 6, "Product card count");
		Assert.assertEquals(products.productNames().size(), 6, "Product name count should match the card count");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "Price high to low puts the most expensive product first")
	public void priceHighToLowPutsDearestFirst() {
		ThreadAuditor.record(ParallelClassLoginTest.SCOPE, "CatalogTest.priceHighToLowPutsDearestFirst");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD)
				.sortBy(SortOption.PRICE_HIGH_TO_LOW);

		List<Double> prices = products.productPrices();
		List<Double> expected = prices.stream().sorted(Comparator.reverseOrder()).toList();

		Assert.assertEquals(prices, expected, "Prices should descend. Actual: " + prices);
		Assert.assertEquals(prices.get(0), prices.stream().max(Double::compare).orElseThrow(),
				"First card should carry the highest price");
	}

	@AfterClass(alwaysRun = true)
	public void reportThread() {
		log.info("{} ran on thread [{}]", getClass().getSimpleName(), Thread.currentThread().getName());
	}
}
