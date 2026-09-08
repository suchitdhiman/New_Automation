package com.sk.tests.parallel;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.TestUser;
import com.sk.pages.CartPage;
import com.sk.pages.ProductsPage;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 3b - {@code parallel="classes"}, part 3 of 3. See
 * {@link ParallelClassLoginTest} for the explanation.
 */
public class ParallelClassCartTest extends BaseWebTest {

	private static final String BACKPACK = "Sauce Labs Backpack";
	private static final String BIKE_LIGHT = "Sauce Labs Bike Light";

	@Test(groups = { Groups.SMOKE, Groups.CART },
			description = "Adding then removing a product returns the badge to empty")
	public void addingThenRemovingClearsTheBadge() {
		ThreadAuditor.record(ParallelClassLoginTest.SCOPE, "CartTest.addingThenRemovingClearsTheBadge");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD).addToCart(BACKPACK);
		Assert.assertEquals(products.cartBadgeCount(), 1, "Badge after adding one product");

		products.removeFromCart(BACKPACK);
		Assert.assertEquals(products.cartBadgeCount(), 0, "Badge should disappear once the cart is empty");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CART },
			description = "The cart page lists exactly the products that were added")
	public void cartPageListsWhatWasAdded() {
		ThreadAuditor.record(ParallelClassLoginTest.SCOPE, "CartTest.cartPageListsWhatWasAdded");

		CartPage cart = loginPage().loginAs(TestUser.STANDARD)
				.addToCart(BACKPACK)
				.addToCart(BIKE_LIGHT)
				.openCart();

		Assert.assertEquals(cart.headerTitle(), "Your Cart", "Cart page header");
		Assert.assertEquals(cart.itemCount(), 2, "Two line items expected");
		Assert.assertTrue(cart.contains(BACKPACK), "Cart should contain " + BACKPACK + ", saw " + cart.itemNames());
		Assert.assertTrue(cart.contains(BIKE_LIGHT), "Cart should contain " + BIKE_LIGHT + ", saw " + cart.itemNames());
	}

	@AfterClass(alwaysRun = true)
	public void reportThreadDistribution() {
		log.info("{} ran on thread [{}]", getClass().getSimpleName(), Thread.currentThread().getName());
		log.info("parallel=classes distribution so far: {}", ThreadAuditor.report(ParallelClassLoginTest.SCOPE));
	}
}
