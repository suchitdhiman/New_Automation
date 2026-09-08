package com.sk.tests.groups;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 4 - grouping, and how the suite XML filters on it.
 *
 * <p>Groups are how one codebase serves several pipelines. Same tests, three
 * different runs:
 *
 * <pre>
 *   &lt;groups&gt;&lt;run&gt;
 *     &lt;include name="smoke"/&gt;                    per-commit, ~1 minute
 *   &lt;/run&gt;&lt;/groups&gt;
 *
 *   &lt;groups&gt;&lt;run&gt;
 *     &lt;include name="regression"/&gt;
 *     &lt;exclude name="flaky"/&gt;                    nightly, minus the known-bad
 *   &lt;/run&gt;&lt;/groups&gt;
 *
 *   &lt;groups&gt;
 *     &lt;define name="release-gate"&gt;               a group of groups
 *       &lt;include name="smoke"/&gt;
 *       &lt;include name="e2e"/&gt;
 *     &lt;/define&gt;
 *     &lt;run&gt;&lt;include name="release-gate"/&gt;&lt;/run&gt;
 *   &lt;/groups&gt;
 * </pre>
 *
 * <p>Two rules that save arguments later:
 * <ul>
 *   <li><b>exclude beats include.</b> A method in both lists does not run.</li>
 *   <li>A method can be in several groups. Tag by <em>speed</em>
 *       ({@code smoke}, {@code regression}) and by <em>area</em>
 *       ({@code catalog}, {@code cart}) and you can slice either way.</li>
 * </ul>
 *
 * <p>{@code @BeforeGroups} / {@code @AfterGroups} bracket a named group: they
 * fire once, around the first and last method of that group, and only if that
 * group is actually running. Use them for setup that a whole area needs but the
 * rest of the suite does not - seeding catalogue data, warming a cache.
 *
 * <p>See {@code suites/04-groups-and-filters.xml}.
 */
public class GroupedCatalogTest extends BaseWebTest {

	private static final AtomicInteger CATALOG_SETUP_COUNT = new AtomicInteger();

	@BeforeGroups(groups = Groups.CATALOG, alwaysRun = true)
	public void beforeCatalogGroup() {
		int calls = CATALOG_SETUP_COUNT.incrementAndGet();
		log.info("@BeforeGroups(catalog) - fired {} time(s). Runs once, before the first catalog test.", calls);
	}

	@AfterGroups(groups = Groups.CATALOG, alwaysRun = true)
	public void afterCatalogGroup() {
		log.info("@AfterGroups(catalog) - the last catalog test has finished.");
	}

	// -- smoke ---------------------------------------------------------------

	@Test(groups = { Groups.SMOKE, Groups.CATALOG },
			description = "SMOKE: the catalogue renders after login")
	public void catalogueRenders() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertTrue(products.isLoaded(), "Catalogue should be on screen");
		Assert.assertEquals(products.headerTitle(), "Products", "Page header");
	}

	@Test(groups = { Groups.SMOKE, Groups.CART },
			description = "SMOKE: a product can be added to the cart")
	public void productCanBeAddedToCart() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD).addToCart("Sauce Labs Backpack");

		Assert.assertEquals(products.cartBadgeCount(), 1, "Cart badge");
	}

	// -- sanity --------------------------------------------------------------

	@Test(groups = { Groups.SANITY, Groups.CATALOG },
			description = "SANITY: every product card carries a non-zero price")
	public void everyProductHasAPrice() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		products.productPrices().forEach(price ->
				Assert.assertTrue(price > 0, "Every product needs a price above zero, saw: " + price));
	}

	// -- regression ----------------------------------------------------------

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "REGRESSION: A to Z is the default sort when the page loads")
	public void defaultSortIsNameAscending() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertEquals(products.selectedSortLabel(), SortOption.NAME_A_TO_Z.label(),
				"Swag Labs opens sorted A to Z");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CART },
			description = "REGRESSION: the cart survives navigating back to the catalogue")
	public void cartSurvivesNavigation() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD).addToCart("Sauce Labs Onesie");

		products = products.openCart().continueShopping();

		Assert.assertEquals(products.cartBadgeCount(), 1,
				"The cart should still hold one item after going back to the catalogue");
	}

	// -- negative ------------------------------------------------------------

	@Test(groups = { Groups.NEGATIVE, Groups.LOGIN },
			description = "NEGATIVE: an unknown username is rejected")
	public void unknownUserIsRejected() {
		loginPage().loginExpectingFailure("no_such_user", "secret_sauce");

		Assert.assertTrue(loginPage().isErrorDisplayed(), "Expected an error banner");
		Assert.assertTrue(loginPage().errorMessage().contains("do not match any user"),
				"Unexpected message: " + loginPage().errorMessage());
	}
}
