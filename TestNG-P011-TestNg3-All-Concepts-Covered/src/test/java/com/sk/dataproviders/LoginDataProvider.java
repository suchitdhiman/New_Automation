package com.sk.dataproviders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.sk.core.ConfigManager;
import com.sk.model.TestUser;

/**
 * Hard-coded login data, showing the four shapes a DataProvider can take.
 *
 * <p>All of them are {@code static}. TestNG can call an instance provider, but
 * then it needs the class to be instantiable and the provider gets tangled up
 * with the test lifecycle. Static providers in a dedicated class are easier to
 * share across test classes via
 * {@code @Test(dataProviderClass = LoginDataProvider.class)}.
 *
 * <h2>Object[][] versus Iterator</h2>
 * {@code Object[][]} builds every row up front - fine for twenty rows. An
 * {@code Iterator<Object[]>} is lazy, so a provider reading ten thousand rows
 * from a database does not have to hold them all in memory at once.
 */
public final class LoginDataProvider {

	private static final Logger LOG = LogManager.getLogger(LoginDataProvider.class);

	private LoginDataProvider() {
		// static provider holder
	}

	/**
	 * The plain form. Each inner array is one invocation, and its elements map
	 * one-to-one onto the test method parameters.
	 */
	@DataProvider(name = "invalidCredentials")
	public static Object[][] invalidCredentials() {
		return new Object[][] {
				{ "TC-L01", "wrong_user", "secret_sauce",
						"Epic sadface: Username and password do not match any user in this service" },
				{ "TC-L02", "standard_user", "wrong_password",
						"Epic sadface: Username and password do not match any user in this service" },
				{ "TC-L03", "", "secret_sauce", "Epic sadface: Username is required" },
				{ "TC-L04", "standard_user", "", "Epic sadface: Password is required" },
				{ "TC-L05", "", "", "Epic sadface: Username is required" },
				{ "TC-L06", "STANDARD_USER", "secret_sauce",
						"Epic sadface: Username and password do not match any user in this service" }
		};
	}

	/**
	 * Same data, lazy delivery, and {@code parallel = true} so the rows run on
	 * separate threads.
	 *
	 * <p>The thread pool for this comes from {@code data-provider-thread-count}
	 * in the suite XML (default 10) - it is a <b>separate</b> pool from
	 * {@code thread-count}. Getting a browser per row only works because
	 * DriverFactory is ThreadLocal.
	 */
	@DataProvider(name = "invalidCredentialsParallel", parallel = true)
	public static Iterator<Object[]> invalidCredentialsParallel() {
		return Arrays.asList(invalidCredentials()).iterator();
	}

	/**
	 * TestNG injects the target {@link Method}, so one provider can serve
	 * several tests and vary what it hands back.
	 */
	@DataProvider(name = "usersForMethod")
	public static Object[][] usersForMethod(Method method) {
		LOG.info("DataProvider [usersForMethod] invoked for [{}]", method.getName());

		if (method.getName().toLowerCase().contains("locked")) {
			return new Object[][] { { TestUser.LOCKED } };
		}
		return new Object[][] { { TestUser.STANDARD }, { TestUser.PROBLEM } };
	}

	/**
	 * TestNG can also inject the {@link ITestContext}, which gives the provider
	 * access to the suite XML - environment, included groups, parameters.
	 *
	 * <p>Used here to keep the slow {@code performance_glitch_user} out of the
	 * smoke run. A row that never should have run is cheaper than a row that
	 * runs and gets skipped.
	 */
	@DataProvider(name = "usersForContext")
	public static Object[][] usersForContext(ITestContext context) {
		boolean smokeRun = Arrays.asList(context.getIncludedGroups()).contains("smoke");
		String environment = ConfigManager.activeEnvironment();

		List<Object[]> rows = new ArrayList<>();
		rows.add(new Object[] { TestUser.STANDARD });
		if (!smokeRun) {
			rows.add(new Object[] { TestUser.PROBLEM });
			rows.add(new Object[] { TestUser.PERFORMANCE });
		}

		LOG.info("DataProvider [usersForContext] built {} row(s) | env={} | smokeRun={}",
				rows.size(), environment, smokeRun);
		return rows.toArray(new Object[0][]);
	}

	/**
	 * {@code indices} tells TestNG to run only those rows. Handy for reproducing
	 * one failing case out of a hundred without editing the data.
	 */
	@DataProvider(name = "firstTwoInvalidCredentials", indices = { 0, 1 })
	public static Object[][] firstTwoInvalidCredentials() {
		return invalidCredentials();
	}
}
