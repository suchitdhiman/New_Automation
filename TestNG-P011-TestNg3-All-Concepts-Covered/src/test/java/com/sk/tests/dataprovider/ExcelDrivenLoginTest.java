package com.sk.tests.dataprovider;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.dataproviders.ExcelDataProvider;
import com.sk.model.LoginScenario;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 9 - Excel-driven testing.
 *
 * <p>The rows come from the {@code LoginData} sheet of
 * {@code src/test/resources/testdata/SwagLabsTestData.xlsx}. Adding a case means
 * adding a row and setting {@code Execute} to Y - no Java, no rebuild of the
 * test logic.
 *
 * <p>A spreadsheet row arrives here as a {@link LoginScenario} record rather
 * than as four loose Strings. That is the difference between
 * {@code scenario.expectedMessage()} and {@code (String) row[4]}.
 *
 * <p>The sheet holds a mix of positive and negative cases and one method handles
 * both, branching on {@code shouldLogin}. That is normally a smell - a test that
 * asserts two different things - but for table-driven data it is the right call:
 * the alternative is two nearly identical methods and two sheets that drift
 * apart.
 */
public class ExcelDrivenLoginTest extends BaseWebTest implements ITest {

	/**
	 * TestNG calls this per invocation, so the report shows "TC-E03
	 * [locked_out_user]" instead of six identical rows.
	 *
	 * <p>The ThreadLocal matters: with a parallel data provider, a plain field
	 * would be overwritten by whichever row started most recently and every row
	 * would be labelled the same.
	 */
	private final ThreadLocal<String> currentTestName = ThreadLocal.withInitial(() -> "excelLogin");

	@Override
	public String getTestName() {
		return currentTestName.get();
	}

	@Test(dataProvider = "excelLoginData", dataProviderClass = ExcelDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.REGRESSION, Groups.LOGIN },
			description = "Login cases driven from the LoginData sheet")
	public void loginBehavesAsTheSheetExpects(LoginScenario scenario) {
		currentTestName.set(scenario.toString());
		log.info("{} | username=[{}] shouldLogin={}", scenario.testCaseId(), scenario.username(),
				scenario.shouldLogin());

		if (scenario.shouldLogin()) {
			ProductsPage products = loginPage().loginAs(scenario.username(), scenario.password());

			Assert.assertTrue(products.isLoaded(),
					scenario.testCaseId() + ": expected a successful sign-in for " + scenario.username());
			Assert.assertEquals(products.headerTitle(), "Products",
					scenario.testCaseId() + ": catalogue header");
		} else {
			loginPage().loginExpectingFailure(scenario.username(), scenario.password());

			Assert.assertTrue(loginPage().isErrorDisplayed(),
					scenario.testCaseId() + ": expected the sign-in to be rejected");
			Assert.assertEquals(loginPage().errorMessage(), scenario.expectedMessage(),
					scenario.testCaseId() + ": error message");
		}
	}

	/**
	 * The same sheet, delivered by a {@code parallel = true} provider. Run it
	 * with {@code suites/08-dataprovider-parallel.xml} and watch the thread
	 * column in the log.
	 */
	@Test(dataProvider = "excelLoginDataParallel", dataProviderClass = ExcelDataProvider.class,
			groups = { Groups.DATA_DRIVEN, Groups.REGRESSION, Groups.LOGIN },
			description = "The same sheet again, rows fanned out across the data-provider thread pool")
	public void loginBehavesAsTheSheetExpectsInParallel(LoginScenario scenario) {
		currentTestName.set(scenario.toString() + " @" + Thread.currentThread().getName());
		log.info("{} running on thread [{}]", scenario.testCaseId(), Thread.currentThread().getName());

		if (scenario.shouldLogin()) {
			Assert.assertTrue(loginPage().loginAs(scenario.username(), scenario.password()).isLoaded(),
					scenario.testCaseId() + ": expected a successful sign-in");
		} else {
			loginPage().loginExpectingFailure(scenario.username(), scenario.password());
			Assert.assertEquals(loginPage().errorMessage(), scenario.expectedMessage(),
					scenario.testCaseId() + ": error message");
		}
	}
}
