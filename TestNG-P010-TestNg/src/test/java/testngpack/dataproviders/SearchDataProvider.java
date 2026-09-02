package testngpack.dataproviders;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

/**
 * Data providers, including the parallel flavour.
 *
 * <p>{@code @DataProvider(parallel = true)} is the fifth kind of parallelism in
 * TestNG (next to methods / classes / tests / instances): one test method is
 * executed concurrently once per data row. Its pool size comes from
 * {@code <suite data-provider-thread-count="..">}.
 */
public class SearchDataProvider {

	@DataProvider(name = "searchTerms")
	public Object[][] searchTerms() {
		return new Object[][] {
				{ "Books", "Harry Potter" },
				{ "Electronics", "Bluetooth headphones" },
				{ "Computers & Accessories", "Mechanical keyboard" },
		};
	}

	@DataProvider(name = "searchTermsParallel", parallel = true)
	public Object[][] searchTermsParallel() {
		return searchTerms();
	}

	/**
	 * Shows the two objects TestNG can inject into a data provider: the
	 * {@link Method} about to run and the {@link ITestContext}, which exposes
	 * the suite xml parameters - the programmatic equivalent of @Optional.
	 */
	@DataProvider(name = "urlKeysParallel", parallel = true)
	public Object[][] urlKeys(Method method, ITestContext context) {
		String extra = context.getCurrentXmlTest().getParameter("extraUrl");
		if (extra != null && !extra.trim().isEmpty()) {
			return new Object[][] { { "amazon" }, { "bbc" }, { extra.trim() } };
		}
		return new Object[][] { { "amazon" }, { "bbc" } };
	}
}
