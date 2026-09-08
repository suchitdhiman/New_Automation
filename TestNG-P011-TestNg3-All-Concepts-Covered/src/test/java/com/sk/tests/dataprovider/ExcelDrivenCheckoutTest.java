package com.sk.tests.dataprovider;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.dataproviders.ExcelDataProvider;
import com.sk.model.CheckoutScenario;
import com.sk.model.TestUser;
import com.sk.pages.CheckoutInformationPage;
import com.sk.pages.CheckoutOverviewPage;

/**
 * CONCEPT 9 (continued) - the shipping form driven from the
 * {@code CheckoutData} sheet.
 *
 * <p>Form validation is where spreadsheet-driven testing genuinely pays for
 * itself. Every combination of missing and present fields is a row; the Java
 * below never changes.
 *
 * <p>Each row starts from a clean browser (fresh driver per {@code @Test} in
 * {@link BaseWebTest}) and walks login to cart to checkout before it gets to
 * the form. That is slower than sharing a session, and it is the reason a failed
 * row tells you about the form rather than about the row that ran before it.
 */
public class ExcelDrivenCheckoutTest extends BaseWebTest {

	private static final String PRODUCT = "Sauce Labs Backpack";

	@Test(dataProvider = "excelCheckoutData", dataProviderClass = ExcelDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.REGRESSION, Groups.CHECKOUT },
			description = "Shipping form validation driven from the CheckoutData sheet")
	public void shippingFormBehavesAsTheSheetExpects(CheckoutScenario scenario) {
		log.info("{} | first=[{}] last=[{}] zip=[{}] shouldSucceed={}",
				scenario.testCaseId(), scenario.firstName(), scenario.lastName(), scenario.zipCode(),
				scenario.shouldSucceed());

		CheckoutInformationPage form = loginPage().loginAs(TestUser.STANDARD)
				.addToCart(PRODUCT)
				.openCart()
				.checkout()
				.enterShippingDetails(scenario);

		if (scenario.shouldSucceed()) {
			CheckoutOverviewPage overview = form.continueToOverview();

			Assert.assertTrue(overview.isLoaded(),
					scenario.testCaseId() + ": valid details should reach the order summary");
			Assert.assertEquals(overview.itemNames().size(), 1,
					scenario.testCaseId() + ": the summary should list the one product we added");
		} else {
			form.continueExpectingRejection();

			Assert.assertTrue(form.isErrorDisplayed(),
					scenario.testCaseId() + ": the form should have been rejected but it was accepted");
			Assert.assertEquals(form.errorMessage(), scenario.expectedError(),
					scenario.testCaseId() + ": validation message");
			Assert.assertTrue(form.currentUrl().contains("checkout-step-one.html"),
					scenario.testCaseId() + ": a rejected form must not navigate away");
		}
	}
}
