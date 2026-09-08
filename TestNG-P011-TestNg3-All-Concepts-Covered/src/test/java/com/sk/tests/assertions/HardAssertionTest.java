package com.sk.tests.assertions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 6a - hard assertions.
 *
 * <p>A hard assertion throws the moment it fails, so nothing after it runs. That
 * is the behaviour you want when everything downstream depends on the check:
 * there is no sense verifying cart totals if login did not work.
 *
 * <h2>The habit worth forming</h2>
 * Every assertion here carries a message. TestNG prints
 * {@code expected [6] but found [5]} on its own, which tells you the numbers and
 * nothing else. {@code "Swag Labs ships six products"} tells the next person
 * what was supposed to be true. It costs four seconds to write and saves twenty
 * minutes when it fails at 3am.
 *
 * <p>Also note the argument order: TestNG is {@code assertEquals(actual,
 * expected)}. JUnit is the other way round. Getting it backwards does not break
 * the test, it just prints a failure message that lies to you.
 */
public class HardAssertionTest extends BaseWebTest {

	@Test(groups = { Groups.SMOKE, Groups.CATALOG },
			description = "assertEquals / assertNotEquals on scalar values")
	public void scalarAssertions() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertEquals(products.headerTitle(), "Products", "Catalogue page header");
		Assert.assertEquals(products.productCount(), 6, "Swag Labs ships six products");
		Assert.assertNotEquals(products.productCount(), 0, "An empty catalogue means the page did not load");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "assertTrue / assertFalse for boolean conditions")
	public void booleanAssertions() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertTrue(products.isLoaded(), "Catalogue should be loaded after a valid login");
		Assert.assertTrue(products.currentUrl().endsWith("inventory.html"),
				"Unexpected landing URL: " + products.currentUrl());
		Assert.assertFalse(products.productNames().isEmpty(), "Product names should not be empty");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "assertNull / assertNotNull, and why assertNotNull comes first")
	public void nullAssertions() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);
		List<String> names = products.productNames();

		// Assert the reference before you assert anything about its contents,
		// otherwise a null turns into an NPE and the report shows a crash
		// instead of a failed expectation.
		Assert.assertNotNull(names, "Product name list");
		Assert.assertEquals(names.size(), 6, "Product names: " + names);
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "assertEquals on collections is order sensitive; assertEqualsNoOrder is not")
	public void collectionAssertions() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		List<String> ascending = products.sortBy(SortOption.NAME_A_TO_Z).productNames();
		List<String> descending = products.sortBy(SortOption.NAME_Z_TO_A).productNames();

		// Same six products, opposite order.
		Assert.assertEqualsNoOrder(ascending.toArray(), descending.toArray(),
				"Sorting must not change WHICH products are shown");

		Assert.assertNotEquals(ascending, descending,
				"...but it must change the order they are shown in");

		List<String> expectedDescending = new ArrayList<>(ascending);
		Collections.reverse(expectedDescending);
		Assert.assertEquals(descending, expectedDescending,
				"Z to A should be exactly the reverse of A to Z");
	}

	@Test(groups = { Groups.NEGATIVE, Groups.LOGIN },
			description = "expectThrows keeps the expectation pinned to one statement")
	public void exceptionAssertions() {
		// The page object refuses to pretend a failed login worked, and says why.
		IllegalStateException thrown = Assert.expectThrows(IllegalStateException.class,
				() -> loginPage().loginAs(TestUser.LOCKED));

		Assert.assertTrue(thrown.getMessage().contains("locked out"),
				"The failure should carry the application message, was: " + thrown.getMessage());
	}

	/**
	 * The defining property of a hard assertion: execution stops at the first
	 * failure, so the second and third problems on the page stay hidden until
	 * you fix the first one and run again.
	 *
	 * <p>{@link com.sk.tests.assertions.SoftAssertionTest} is the answer to that.
	 * In {@link Groups#DEMO_STATUS} so it only runs in the showcase suite.
	 */
	@Test(groups = { Groups.CATALOG, Groups.DEMO_STATUS },
			description = "FAILS ON PURPOSE - shows that a hard assertion hides every later check")
	public void demoHardAssertionStopsAtTheFirstFailure() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertEquals(products.productCount(), 99,
				"Deliberately wrong: the catalogue has 6 products, not 99");

		Assert.fail("Unreachable - the assertion above already threw. That is the whole point.");
	}
}
