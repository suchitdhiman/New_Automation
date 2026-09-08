package com.sk.tests.parallel;

import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.sk.core.BaseWebTest;
import com.sk.core.Groups;
import com.sk.model.SortOption;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;
import com.sk.utils.ThreadAuditor;

/**
 * CONCEPT 3c - {@code parallel="instances"}, driven by a {@code @Factory}.
 *
 * <p>A {@code @Factory} constructor turns one class into several test
 * <em>instances</em>, each holding different state. Here, one instance per sort
 * option: four instances, each running the same two test methods against its
 * own {@link SortOption}.
 *
 * <p>With {@code parallel="instances"} TestNG gives each instance a thread. So
 * this is the mode to reach for when the thing you want to fan out is a
 * <b>data-carrying object</b>, not a method and not a class.
 *
 * <h2>Factory versus DataProvider</h2>
 * They look similar and solve different problems:
 * <ul>
 *   <li>A <b>DataProvider</b> varies the arguments of <em>one method</em>. The
 *       instance is shared.</li>
 *   <li>A <b>Factory</b> varies the <em>object</em>, so every method in the
 *       class - and every {@code @BeforeClass} - sees that instance state.</li>
 * </ul>
 * Reach for a factory when the parameter belongs to the whole class.
 *
 * <p>Implementing {@link ITest} is what stops the report showing four identical
 * rows called {@code sortingProducesTheExpectedOrder}.
 */
public class ParallelInstancesTest extends BaseWebTest implements ITest {

	private static final String SCOPE = "parallel-instances";

	private final SortOption sortOption;

	@Factory(dataProvider = "sortOptions")
	public ParallelInstancesTest(SortOption sortOption) {
		this.sortOption = sortOption;
	}

	/** The factory data. One row here is one instance of this class. */
	@DataProvider(name = "sortOptions")
	public static Object[][] sortOptions() {
		return new Object[][] {
				{ SortOption.NAME_A_TO_Z },
				{ SortOption.NAME_Z_TO_A },
				{ SortOption.PRICE_LOW_TO_HIGH },
				{ SortOption.PRICE_HIGH_TO_LOW }
		};
	}

	@Override
	public String getTestName() {
		return "sort=" + sortOption.value();
	}

	@Test(groups = { Groups.REGRESSION, Groups.CATALOG },
			description = "Each factory instance verifies one sort option end to end")
	public void sortingProducesTheExpectedOrder() {
		ThreadAuditor.record(SCOPE, "sort-" + sortOption.value());
		log.info("Instance [{}] running on thread [{}]", getTestName(), Thread.currentThread().getName());

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD).sortBy(sortOption);

		Assert.assertEquals(products.selectedSortLabel(), sortOption.label(),
				"Dropdown should show the option we selected");

		switch (sortOption) {
			case NAME_A_TO_Z -> assertOrdered(products.productNames(), Comparator.naturalOrder());
			case NAME_Z_TO_A -> assertOrdered(products.productNames(), Comparator.reverseOrder());
			case PRICE_LOW_TO_HIGH -> assertOrdered(products.productPrices(), Comparator.naturalOrder());
			case PRICE_HIGH_TO_LOW -> assertOrdered(products.productPrices(), Comparator.reverseOrder());
		}
	}

	@Test(groups = { Groups.SANITY, Groups.CATALOG },
			description = "Sorting never changes how many products are on the page")
	public void sortingDoesNotChangeTheProductCount() {
		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);
		int before = products.productCount();

		products.sortBy(sortOption);

		Assert.assertEquals(products.productCount(), before,
				"Sorting by " + sortOption.label() + " should reorder products, not add or drop any");
	}

	@AfterClass(alwaysRun = true)
	public void reportThreadDistribution() {
		log.info("parallel=instances distribution: {}", ThreadAuditor.report(SCOPE));
	}

	private <T> void assertOrdered(List<T> actual, Comparator<? super T> comparator) {
		List<T> expected = actual.stream().sorted(comparator).toList();
		Assert.assertEquals(actual, expected,
				"Order for " + sortOption.label() + " was wrong. Actual: " + actual);
	}
}
