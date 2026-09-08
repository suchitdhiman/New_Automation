package com.sk.tests.assertions;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.CheckoutOverviewPage;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 6b - soft assertions.
 *
 * <p>A {@link SoftAssert} records failures instead of throwing, and reports all
 * of them when {@code assertAll()} is called. One run tells you all four things
 * that are wrong on the page, instead of the first one, four times, over four
 * afternoons.
 *
 * <h2>Two traps, both handled here</h2>
 * <ol>
 *   <li><b>Forgetting {@code assertAll()}.</b> The test passes. Silently. This
 *       is the single most common way a suite ends up green and worthless.
 *       Every test below ends with an explicit {@code assertAll()}, and the
 *       {@code @AfterMethod} logs an ERROR if one was missed.</li>
 *   <li><b>Sharing one SoftAssert across threads.</b> A plain instance field
 *       collects failures from every parallel test at once and attributes them
 *       to whichever one calls {@code assertAll()} first. A {@link ThreadLocal}
 *       gives each thread its own collector.</li>
 * </ol>
 *
 * <p>You will also see {@code assertAll()} called from an {@code @AfterMethod}
 * so individual tests cannot forget it. It reads well, but exactly how TestNG
 * reports an exception thrown from a configuration method has moved around
 * between versions, so this class keeps the call inside the test where the
 * result is unambiguous.
 *
 * <h2>When to use which</h2>
 * Soft for <em>independent</em> observations about one screen. Hard for anything
 * the rest of the test depends on - if login failed, do not soft-assert your way
 * through six more checks against a login page.
 */
public class SoftAssertionTest extends BaseWebTest {

	private final ThreadLocal<SoftAssert> softAssert = new ThreadLocal<>();
	private final ThreadLocal<Boolean> asserted = ThreadLocal.withInitial(() -> Boolean.FALSE);

	@BeforeMethod(alwaysRun = true)
	public void newSoftAssertForThisThread() {
		softAssert.set(new SoftAssert());
		asserted.set(Boolean.FALSE);
	}

	@AfterMethod(alwaysRun = true)
	public void warnIfAssertAllWasForgotten() {
		if (Boolean.FALSE.equals(asserted.get())) {
			log.error("This test collected soft assertions but never called assertAll() - "
					+ "any failure it found was thrown away. Fix the test.");
		}
		softAssert.remove();
		asserted.remove();
	}

	private SoftAssert softly() {
		return softAssert.get();
	}

	/** Marks the collector as consumed, then reports everything it gathered. */
	private void assertAll(String context) {
		asserted.set(Boolean.TRUE);
		softAssert.get().assertAll(context);
	}

	@Test(groups = { Groups.SMOKE, Groups.CATALOG },
			description = "Validates the whole catalogue in one pass and reports every problem at once")
	public void catalogueIsFullyValid() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		// Hard assertion: nothing below is meaningful if we are not on the catalogue.
		Assert.assertTrue(products.isLoaded(), "Catalogue must be loaded before validating it");

		SoftAssert softly = softly();
		softly.assertEquals(products.headerTitle(), "Products", "Page header");
		softly.assertEquals(products.productCount(), 6, "Product card count");
		softly.assertEquals(products.selectedSortLabel(), SortOption.NAME_A_TO_Z.label(), "Default sort");
		softly.assertEquals(products.cartBadgeCount(), 0, "A fresh session should have an empty cart");

		List<Double> prices = products.productPrices();
		softly.assertEquals(prices.size(), 6, "Every card should carry a price");
		prices.forEach(price -> softly.assertTrue(price > 0, "Price should be above zero, saw " + price));

		List<String> names = products.productNames();
		names.forEach(name -> softly.assertFalse(name.isBlank(), "Product names must not be blank"));
		softly.assertEquals((long) names.size(), names.stream().distinct().count(),
				"Product names should be unique. Saw: " + names);

		assertAll("Catalogue validation");
	}

	@Test(groups = { Groups.REGRESSION, Groups.CHECKOUT },
			description = "Checks every number on the order summary before deciding whether the maths is right")
	public void orderSummaryArithmeticIsConsistent() {
		CheckoutOverviewPage overview = loginPage().loginAs(TestUser.STANDARD)
				.addToCart("Sauce Labs Backpack")
				.addToCart("Sauce Labs Fleece Jacket")
				.openCart()
				.checkout()
				.enterShippingDetails("Ayush", "Dhiman", "560037")
				.continueToOverview();

		double itemTotal = overview.itemTotal();
		double tax = overview.tax();
		double grandTotal = overview.grandTotal();
		log.info("Summary values: items={} tax={} total={}", itemTotal, tax, grandTotal);

		SoftAssert softly = softly();
		softly.assertEquals(overview.itemNames().size(), 2, "Two products should be listed on the summary");
		softly.assertTrue(itemTotal > 0, "Item total should be positive, was " + itemTotal);
		softly.assertTrue(tax > 0, "Tax should be positive, was " + tax);
		softly.assertEquals(grandTotal, itemTotal + tax, 0.001, "Grand total should be item total plus tax");
		softly.assertEquals(tax, round(itemTotal * 0.08), 0.01,
				"Swag Labs charges 8% tax; expected " + round(itemTotal * 0.08) + " but the page showed " + tax);

		assertAll("Order summary validation");
	}

	/**
	 * Deliberately fails three separate checks. The report shows all three in one
	 * message, which is exactly what a hard assertion could not do.
	 *
	 * <p>In {@link Groups#DEMO_STATUS} so it only runs in the showcase suite.
	 */
	@Test(groups = { Groups.CATALOG, Groups.DEMO_STATUS },
			description = "FAILS ON PURPOSE - three soft failures reported together")
	public void demoSoftAssertionsCollectEveryFailure() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		SoftAssert softly = softly();
		softly.assertEquals(products.headerTitle(), "Our Products", "Deliberately wrong header text");
		softly.assertEquals(products.productCount(), 99, "Deliberately wrong product count");
		softly.assertEquals(products.cartBadgeCount(), 5, "Deliberately wrong cart badge");

		log.warn("Three assertions have already failed and execution still reached this line. "
				+ "Nothing is reported until assertAll() runs.");

		assertAll("Deliberate soft assertion demo");
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
