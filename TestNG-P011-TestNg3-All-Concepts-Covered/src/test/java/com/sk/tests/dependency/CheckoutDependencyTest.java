package com.sk.tests.dependency;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.Browser;
import com.sk.core.ConfigManager;
import com.sk.core.DriverFactory;
import com.sk.core.Groups;
import com.sk.model.TestUser;
import com.sk.pages.CartPage;
import com.sk.pages.CheckoutCompletePage;
import com.sk.pages.CheckoutInformationPage;
import com.sk.pages.CheckoutOverviewPage;
import com.sk.pages.LoginPage;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 5a - {@code dependsOnMethods}: a purchase journey split into steps.
 *
 * <p>Six methods, each depending on the one before. If {@code signIn} fails,
 * TestNG marks the other five <b>SKIPPED</b> rather than running them and
 * producing five identical, useless failures. That is the entire value of
 * {@code dependsOnMethods}: one root cause, one red line, five honest skips.
 *
 * <h2>Why this class manages its own browser</h2>
 * {@link com.sk.core.BaseWebTest} opens a fresh driver per {@code @Test}, which
 * is the right default. A dependency chain is the exception - the whole point is
 * that step 4 sees the cart step 2 filled - so the driver is opened in
 * {@code @BeforeClass} and closed in {@code @AfterClass}, and the page objects
 * are class fields.
 *
 * <p><b>Do not run this class under {@code parallel="methods"}.</b> The chain
 * needs one browser on one thread. {@code parallel="classes"} or
 * {@code parallel="instances"} are both fine, because they keep a class on a
 * single thread; see {@code suites/05-dependencies.xml}.
 *
 * <h2>dependsOnMethods is not priority</h2>
 * {@code priority} only orders. {@code dependsOnMethods} orders <em>and</em>
 * propagates failure as a skip. If the second thing is pointless when the first
 * one broke, you want a dependency, not a priority.
 */
public class CheckoutDependencyTest extends BaseTest {

	private static final String BACKPACK = "Sauce Labs Backpack";
	private static final String BIKE_LIGHT = "Sauce Labs Bike Light";
	private static final String FIRST_NAME = "Ayush";
	private static final String LAST_NAME = "Dhiman";
	private static final String POSTCODE = "560037";

	private WebDriver driver;
	private ProductsPage products;
	private CartPage cart;
	private CheckoutInformationPage checkoutInformation;
	private CheckoutOverviewPage overview;

	private double expectedItemTotal;

	@BeforeClass(alwaysRun = true)
	@Parameters({ "browser" })
	public void openBrowserForTheJourney(@Optional String browserFromXml) {
		String requested = (browserFromXml == null || browserFromXml.isBlank())
				? ConfigManager.get("browser")
				: browserFromXml;

		driver = DriverFactory.init(Browser.from(requested));
		driver.get(ConfigManager.baseUrl());
	}

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		DriverFactory.quit();
	}

	// -- the chain -----------------------------------------------------------

	@Test(priority = 1, groups = { Groups.E2E, Groups.LOGIN },
			description = "Step 1 - sign in. Every other step in this class depends on it.")
	public void signIn() {
		products = new LoginPage(driver).loginAs(TestUser.STANDARD);

		Assert.assertTrue(products.isLoaded(), "Sign-in should land on the catalogue");
	}

	@Test(dependsOnMethods = "signIn", groups = { Groups.E2E, Groups.CART },
			description = "Step 2 - add two products and remember what they cost")
	public void addProductsToCart() {
		products.addToCart(BACKPACK).addToCart(BIKE_LIGHT);

		Assert.assertEquals(products.cartBadgeCount(), 2, "Cart badge after adding two products");
	}

	@Test(dependsOnMethods = "addProductsToCart", groups = { Groups.E2E, Groups.CART },
			description = "Step 3 - the cart page lists both products")
	public void openCartAndVerifyContents() {
		cart = products.openCart();
		expectedItemTotal = cart.itemPrices().stream().mapToDouble(Double::doubleValue).sum();

		Assert.assertEquals(cart.itemCount(), 2, "Line items in the cart");
		Assert.assertTrue(cart.contains(BACKPACK), "Cart contents: " + cart.itemNames());
		Assert.assertTrue(cart.contains(BIKE_LIGHT), "Cart contents: " + cart.itemNames());
	}

	@Test(dependsOnMethods = "openCartAndVerifyContents", groups = { Groups.E2E, Groups.CHECKOUT },
			description = "Step 4 - shipping details are accepted")
	public void enterShippingDetails() {
		checkoutInformation = cart.checkout();
		overview = checkoutInformation
				.enterShippingDetails(FIRST_NAME, LAST_NAME, POSTCODE)
				.continueToOverview();

		Assert.assertTrue(overview.isLoaded(), "Valid details should move us to the order summary");
	}

	@Test(dependsOnMethods = "enterShippingDetails", groups = { Groups.E2E, Groups.CHECKOUT },
			description = "Step 5 - the summary totals add up")
	public void reviewOrderTotals() {
		double itemTotal = overview.itemTotal();
		double tax = overview.tax();
		double grandTotal = overview.grandTotal();

		log.info("Summary: items={} tax={} total={}", itemTotal, tax, grandTotal);

		Assert.assertEquals(itemTotal, expectedItemTotal, 0.001,
				"Item total should equal the sum of the cart line prices");
		Assert.assertEquals(grandTotal, itemTotal + tax, 0.001,
				"Grand total should be item total plus tax");
	}

	@Test(dependsOnMethods = "reviewOrderTotals", groups = { Groups.E2E, Groups.CHECKOUT },
			description = "Step 6 - the order completes")
	public void completeOrder() {
		CheckoutCompletePage complete = overview.finish();

		Assert.assertEquals(complete.confirmationHeader(), CheckoutCompletePage.EXPECTED_HEADER,
				"Order confirmation header");
		Assert.assertTrue(complete.currentUrl().contains("checkout-complete.html"),
				"Should be on the confirmation URL, was: " + complete.currentUrl());
	}

	/**
	 * {@code alwaysRun = true} on a <em>test</em> method means "run me even if
	 * something I depend on failed". Useful for a cleanup-style check that must
	 * report regardless. Without it, this would be skipped along with the rest
	 * of the chain.
	 */
	@Test(dependsOnMethods = "completeOrder", alwaysRun = true, groups = { Groups.E2E, Groups.CHECKOUT },
			description = "Step 7 - runs even if the chain broke, because alwaysRun=true")
	public void sessionIsStillUsableAfterTheJourney() {
		Assert.assertNotNull(driver.getCurrentUrl(), "The browser session should still be alive");
		log.info("Journey finished on {}", driver.getCurrentUrl());
	}
}
