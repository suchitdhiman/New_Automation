package com.sk.tests.parallel;

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
 * CONCEPT 3a - {@code parallel="methods"}.
 *
 * <p>Every {@code @Test} method in the run is handed to the thread pool
 * independently. Four methods with {@code thread-count="4"} means four browsers
 * open at once, even though they all belong to this one class.
 *
 * <p>Run it with {@code suites/03-parallel-methods.xml} and watch the {@code [%t]}
 * column in the log: the method names interleave.
 *
 * <h2>The rule this mode enforces on you</h2>
 * Methods must be genuinely independent. There is no ordering, no shared state,
 * no "this one logs in and the next one uses the session". Each method here does
 * its own login, which is exactly why it is safe to fan them out.
 *
 * <p>{@code @AfterClass} prints the thread distribution rather than asserting
 * it. Whether the pool actually used four distinct threads depends on timing
 * and the size of the pool; asserting on that would be a flaky test about
 * flakiness. The assertion that <em>is</em> safe lives in
 * {@link com.sk.tests.execution.InvocationAndThreadPoolTest}, where
 * {@code threadPoolSize} guarantees a pool.
 */
public class ParallelMethodsTest extends BaseWebTest {

	private static final String SCOPE = "parallel-methods";

	@Test(groups = { Groups.SMOKE, Groups.CATALOG },
			description = "Catalogue shows all six products after a standard login")
	public void catalogueShowsEveryProduct() {
		ThreadAuditor.record(SCOPE, "catalogueShowsEveryProduct");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertEquals(products.headerTitle(), "Products", "Landing page header");
		Assert.assertEquals(products.productCount(), 6, "Swag Labs ships six products");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "Sorting by name Z to A reverses the alphabetical order")
	public void sortByNameDescending() {
		ThreadAuditor.record(SCOPE, "sortByNameDescending");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD)
				.sortBy(SortOption.NAME_Z_TO_A);

		var names = products.productNames();
		var expected = names.stream().sorted(java.util.Comparator.reverseOrder()).toList();

		Assert.assertEquals(products.selectedSortLabel(), SortOption.NAME_Z_TO_A.label(), "Dropdown label");
		Assert.assertEquals(names, expected, "Products should be in reverse alphabetical order");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "Sorting by price low to high orders the prices ascending")
	public void sortByPriceAscending() {
		ThreadAuditor.record(SCOPE, "sortByPriceAscending");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD)
				.sortBy(SortOption.PRICE_LOW_TO_HIGH);

		var prices = products.productPrices();
		var expected = prices.stream().sorted().toList();

		Assert.assertEquals(prices, expected, "Prices should ascend. Actual: " + prices);
	}

	@Test(groups = { Groups.SMOKE, Groups.CART },
			description = "Adding two products puts 2 on the cart badge")
	public void cartBadgeCountsAddedProducts() {
		ThreadAuditor.record(SCOPE, "cartBadgeCountsAddedProducts");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD)
				.addToCart("Sauce Labs Backpack")
				.addToCart("Sauce Labs Bike Light");

		Assert.assertEquals(products.cartBadgeCount(), 2, "Cart badge after adding two products");
	}

	@AfterClass(alwaysRun = true)
	public void reportThreadDistribution() {
		log.info("parallel=methods distribution: {}", ThreadAuditor.report(SCOPE));
	}
}
