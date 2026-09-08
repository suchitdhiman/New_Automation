package com.sk.tests.parallel;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 3b - {@code parallel="classes"}, part 1 of 3.
 *
 * <p>With this mode TestNG gives each <b>class</b> a thread. The methods inside
 * a class still run one after another, on that same thread.
 *
 * <p>That makes it the right choice when methods inside a class do share state -
 * a class-level login in {@code @BeforeClass}, a record created by the first
 * test and read by the second - but different classes do not. It is also the
 * gentler mode: three classes means three browsers, not three browsers per
 * class.
 *
 * <p>This class, {@link ParallelClassCatalogTest} and {@link ParallelClassCartTest}
 * are run together by {@code suites/03-parallel-classes.xml}.
 */
public class ParallelClassLoginTest extends BaseWebTest {

	static final String SCOPE = "parallel-classes";

	@Test(groups = { Groups.SMOKE, Groups.LOGIN },
			description = "A valid standard user reaches the product catalogue")
	public void standardUserCanSignIn() {
		ThreadAuditor.record(SCOPE, "LoginTest.standardUserCanSignIn");

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		Assert.assertTrue(products.isLoaded(), "Expected to land on the inventory page");
		Assert.assertTrue(products.currentUrl().contains("inventory.html"), "URL after login: " + products.currentUrl());
	}

	@Test(groups = { Groups.NEGATIVE, Groups.LOGIN },
			description = "The locked-out account is rejected with the documented message")
	public void lockedOutUserIsRejected() {
		ThreadAuditor.record(SCOPE, "LoginTest.lockedOutUserIsRejected");

		loginPage().loginExpectingFailure(TestUser.LOCKED.username(), TestUser.LOCKED.password());

		Assert.assertTrue(loginPage().isErrorDisplayed(), "An error banner was expected");
		Assert.assertEquals(loginPage().errorMessage(),
				"Epic sadface: Sorry, this user has been locked out.",
				"Lock-out message text");
	}

	@AfterClass(alwaysRun = true)
	public void reportThread() {
		log.info("{} ran on thread [{}]", getClass().getSimpleName(), Thread.currentThread().getName());
	}
}
