package com.sk.tests.e2e;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.CartPage;
import com.sk.pages.CheckoutCompletePage;
import com.sk.pages.CheckoutOverviewPage;
import com.sk.pages.LoginPage;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 11 - the Page Object Model, doing the job it exists for.
 *
 * <p>Read {@link #customerCanBuyTwoProductsEndToEnd()} top to bottom. There is
 * not a single locator, wait or {@code driver.findElement} in it. It reads like
 * the test case a manual tester would have written, because every page hands
 * back the page you land on next and the mechanics stay in
 * {@code com.sk.pages}.
 *
 * <h2>The rules this framework follows</h2>
 * <ul>
 *   <li><b>A page action returns the resulting page.</b> {@code cart.checkout()}
 *       gives you a {@code CheckoutInformationPage}. The compiler then stops you
 *       calling a checkout method while you are still on the cart.</li>
 *   <li><b>Page objects assert nothing.</b> They expose state; the test decides
 *       what is correct. A page that asserts cannot be reused by a test with
 *       different expectations.</li>
 *   <li><b>Page objects return domain types.</b> {@code double} for a price, not
 *       the string {@code "Total: $32.39"}. Parsing is the page object's job.</li>
 *   <li><b>No {@code Thread.sleep} anywhere.</b> Every wait is an explicit
 *       condition in {@code BasePage}.</li>
 * </ul>
 */
public class EndToEndPurchaseTest extends BaseWebTest {

	private static final String BACKPACK = "Sauce Labs Backpack";
	private static final String BOLT_TSHIRT = "Sauce Labs Bolt T-Shirt";
	private static final double TAX_RATE = 0.08;

	@Test(groups = { Groups.E2E, Groups.SMOKE, Groups.CHECKOUT },
			description = "A customer signs in, buys two products and receives an order confirmation")
	public void customerCanBuyTwoProductsEndToEnd() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);
		Assert.assertTrue(products.isLoaded(), "Sign-in should land on the catalogue");

		products.sortBy(SortOption.PRICE_LOW_TO_HIGH)
				.addToCart(BACKPACK)
				.addToCart(BOLT_TSHIRT);
		Assert.assertEquals(products.cartBadgeCount(), 2, "Cart badge after adding two products");

		CartPage cart = products.openCart();
		Assert.assertEquals(cart.itemCount(), 2, "Line items in the cart");
		double expectedItemTotal = cart.itemPrices().stream().mapToDouble(Double::doubleValue).sum();

		CheckoutOverviewPage overview = cart.checkout()
				.enterShippingDetails("Ayush", "Dhiman", "560037")
				.continueToOverview();

		SoftAssert softly = new SoftAssert();
		softly.assertEquals(overview.itemNames().size(), 2, "Products listed on the summary");
		softly.assertEquals(overview.itemTotal(), expectedItemTotal, 0.001,
				"Item total should match the sum of the cart prices");
		softly.assertEquals(overview.tax(), round(expectedItemTotal * TAX_RATE), 0.01,
				"Tax should be 8% of the item total");
		softly.assertEquals(overview.grandTotal(), overview.itemTotal() + overview.tax(), 0.001,
				"Grand total should be item total plus tax");
		softly.assertAll("Order summary");

		CheckoutCompletePage confirmation = overview.finish();
		Assert.assertEquals(confirmation.confirmationHeader(), CheckoutCompletePage.EXPECTED_HEADER,
				"Order confirmation header");
		Assert.assertTrue(confirmation.isConfirmationImageShown(), "The confirmation graphic should be shown");
	}

	@Test(groups = { Groups.E2E, Groups.REGRESSION, Groups.CART },
			description = "Removing a product from the cart page updates the cart everywhere")
	public void removingAProductUpdatesTheCart() {
		CartPage cart = loginPage().loginAs(TestUser.STANDARD)
				.addToCart(BACKPACK)
				.addToCart(BOLT_TSHIRT)
				.openCart();
		Assert.assertEquals(cart.itemCount(), 2, "Two products to start with");

		cart.removeItem(BACKPACK);

		Assert.assertEquals(cart.itemCount(), 1, "One product should remain");
		Assert.assertFalse(cart.contains(BACKPACK), "The removed product should be gone: " + cart.itemNames());

		ProductsPage products = cart.continueShopping();
		Assert.assertEquals(products.cartBadgeCount(), 1, "The badge should agree with the cart page");
	}

	@Test(groups = { Groups.E2E, Groups.REGRESSION, Groups.CHECKOUT },
			description = "Cancelling at the order summary returns to the catalogue with the cart intact")
	public void cancellingCheckoutKeepsTheCart() {
		CheckoutOverviewPage overview = loginPage().loginAs(TestUser.STANDARD)
				.addToCart(BACKPACK)
				.openCart()
				.checkout()
				.enterShippingDetails("Ayush", "Dhiman", "560037")
				.continueToOverview();

		ProductsPage products = overview.cancel();

		Assert.assertTrue(products.isLoaded(), "Cancel should return to the catalogue");
		Assert.assertEquals(products.cartBadgeCount(), 1,
				"Abandoning checkout must not empty the cart - the customer may come back to it");
	}

	@Test(groups = { Groups.E2E, Groups.REGRESSION, Groups.LOGIN },
			description = "Signing out returns to the login screen and ends the session")
	public void signingOutEndsTheSession() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		LoginPage afterLogout = products.logout();

		Assert.assertTrue(afterLogout.isLoaded(), "Logout should return to the sign-in screen");
		Assert.assertTrue(afterLogout.showsPublishedCredentials(),
				"The login page should be fully rendered after signing out");
	}

	@Test(groups = { Groups.E2E, Groups.REGRESSION, Groups.CATALOG },
			description = "Sorting and adding to the cart do not interfere with each other")
	public void sortingDoesNotDisturbTheCart() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD).addToCart(BACKPACK);

		List<String> before = products.productNames();
		products.sortBy(SortOption.PRICE_HIGH_TO_LOW);

		Assert.assertEquals(products.cartBadgeCount(), 1, "Re-sorting must not empty the cart");
		Assert.assertEquals(products.productNames().size(), before.size(),
				"Re-sorting must not add or drop products");
		Assert.assertTrue(products.openCart().contains(BACKPACK),
				"The product added before sorting should still be in the cart");
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
