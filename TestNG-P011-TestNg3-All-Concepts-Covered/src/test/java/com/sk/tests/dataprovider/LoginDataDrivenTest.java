package com.sk.tests.dataprovider;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.dataproviders.LoginDataProvider;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 8b - DataProviders, every shape of them.
 *
 * <p>All four providers live in {@link LoginDataProvider} and are pulled in with
 * {@code dataProviderClass}. Keeping them out of the test class means two things:
 * other classes can reuse them, and the test class stays about assertions.
 *
 * <h2>Reading the report</h2>
 * Each row becomes its own result. That is why the first parameter of every
 * method here is a test-case id - it lands in the report and in the Excel
 * output, so "TC-L03 failed" is actionable without opening the code.
 *
 * <h2>Parallel data providers</h2>
 * {@code @DataProvider(parallel = true)} uses the
 * {@code data-provider-thread-count} pool, which is <b>separate</b> from the
 * {@code thread-count} pool that {@code parallel="methods"} uses. Six rows on
 * six threads means six browsers; that only works because
 * {@code DriverFactory} is ThreadLocal. See
 * {@code suites/08-dataprovider-parallel.xml}.
 */
public class LoginDataDrivenTest extends BaseWebTest {

	@Test(dataProvider = "invalidCredentials", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.NEGATIVE, Groups.LOGIN },
			description = "Every invalid credential combination shows the right error")
	public void invalidCredentialsAreRejected(String testCaseId, String username, String password,
			String expectedMessage) {

		log.info("{} : signing in with username=[{}] password=[{}]", testCaseId, username,
				password.isEmpty() ? "" : "***");

		loginPage().loginExpectingFailure(username, password);

		Assert.assertTrue(loginPage().isErrorDisplayed(), testCaseId + " expected an error banner");
		Assert.assertEquals(loginPage().errorMessage(), expectedMessage, testCaseId + " error message");
	}

	/** Same data, same assertions, delivered on the data-provider thread pool. */
	@Test(dataProvider = "invalidCredentialsParallel", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.NEGATIVE, Groups.LOGIN },
			description = "The same rows again, this time running in parallel")
	public void invalidCredentialsAreRejectedInParallel(String testCaseId, String username, String password,
			String expectedMessage) {

		log.info("{} running on thread [{}]", testCaseId, Thread.currentThread().getName());

		loginPage().loginExpectingFailure(username, password);

		Assert.assertEquals(loginPage().errorMessage(), expectedMessage, testCaseId + " error message");
	}

	/**
	 * The provider inspects the {@code Method} it is feeding. This method name
	 * does not contain "locked", so it gets the accounts that can sign in.
	 */
	@Test(dataProvider = "usersForMethod", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.LOGIN },
			description = "Method-aware provider: accounts that are expected to sign in")
	public void validAccountsReachTheCatalogue(TestUser user) {
		ProductsPage products = loginPage().loginAs(user);

		Assert.assertTrue(products.isLoaded(), user.username() + " should reach the catalogue");
		Assert.assertEquals(products.productCount(), 6, user.username() + " should see six products");
	}

	/** Same provider. The word "locked" in the method name changes what it returns. */
	@Test(dataProvider = "usersForMethod", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.NEGATIVE, Groups.LOGIN },
			description = "Method-aware provider: the same provider hands this method the locked account")
	public void lockedAccountsAreRefused(TestUser user) {
		Assert.assertEquals(user, TestUser.LOCKED,
				"The provider should have detected 'locked' in the method name and sent the locked account");

		loginPage().loginExpectingFailure(user.username(), user.password());

		Assert.assertTrue(loginPage().errorMessage().contains("locked out"),
				"Unexpected message: " + loginPage().errorMessage());
	}

	/**
	 * The provider reads the {@code ITestContext}, so the rows it produces depend
	 * on which groups the suite asked for - one row in a smoke run, three in a
	 * regression run.
	 */
	@Test(dataProvider = "usersForContext", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.REGRESSION, Groups.LOGIN },
			description = "Context-aware provider: the row count depends on the suite configuration")
	public void contextAwareAccountsCanSignIn(TestUser user) {
		ProductsPage products = loginPage().loginAs(user);

		Assert.assertTrue(products.isLoaded(), user.username() + " should reach the catalogue");
	}

	/**
	 * {@code indices = {0, 1}} on the provider means only the first two rows run.
	 * Useful when you want to reproduce one case out of a hundred without
	 * touching the data.
	 */
	@Test(dataProvider = "firstTwoInvalidCredentials", dataProviderClass = LoginDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.SANITY, Groups.LOGIN },
			description = "indices on a provider run a subset of the rows")
	public void onlyTheFirstTwoRowsRun(String testCaseId, String username, String password, String expectedMessage) {
		Assert.assertTrue(testCaseId.equals("TC-L01") || testCaseId.equals("TC-L02"),
				"indices={0,1} should limit this to the first two rows, but got " + testCaseId);

		loginPage().loginExpectingFailure(username, password);
		Assert.assertEquals(loginPage().errorMessage(), expectedMessage, testCaseId + " error message");
	}
}
