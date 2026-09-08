package com.sk.tests.retry;

import java.time.Duration;
import java.time.Instant;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sk.annotations.Flaky;
import com.sk.core.BaseWebTest;
import com.sk.core.ConfigManager;
import com.sk.core.Groups;
import com.sk.model.TestUser;
import com.sk.pages.ProductsPage;

/**
 * CONCEPT 8a (continued) - retry against the real application.
 *
 * <p>Swag Labs ships a {@code performance_glitch_user} whose login is
 * deliberately slow. That is the honest version of a flaky test: nothing is
 * broken, the environment is just slower than the timeout you picked.
 *
 * <p>Marked {@link Flaky} with the reason spelled out. On a real project that
 * annotation should carry a ticket number and get deleted when the ticket
 * closes - otherwise "retry twice" quietly becomes the team's definition of
 * working.
 */
public class SlowUserRetryTest extends BaseWebTest {

	@Flaky(maxRetries = 2, reason = "performance_glitch_user can exceed the explicit wait on a loaded machine")
	@Test(groups = { Groups.FLAKY, Groups.REGRESSION, Groups.LOGIN },
			description = "The deliberately slow account still reaches the catalogue, retrying if the wait is exceeded")
	public void slowAccountStillReachesTheCatalogue() {
		Instant started = Instant.now();

		ProductsPage products = loginPage().loginAs(TestUser.PERFORMANCE);

		Duration took = Duration.between(started, Instant.now());
		log.info("performance_glitch_user signed in after {} ms", took.toMillis());

		Assert.assertTrue(products.isLoaded(), "The slow account should still land on the catalogue");
		Assert.assertEquals(products.productCount(), 6, "It should see the same six products as everyone else");
		Assert.assertTrue(took.toSeconds() <= ConfigManager.getInt("explicit.wait"),
				"Login took " + took.toSeconds() + "s, which is over the configured explicit.wait of "
						+ ConfigManager.getInt("explicit.wait") + "s");
	}

	/**
	 * The counterpart, and the more important test of the two: the standard
	 * account must never need a retry. If this one ever goes flaky, that is a
	 * real defect and no annotation should hide it.
	 */
	@Test(groups = { Groups.SMOKE, Groups.LOGIN },
			description = "The standard account is fast and is deliberately NOT marked flaky")
	public void standardAccountIsNotFlaky() {
		Instant started = Instant.now();

		ProductsPage products = loginPage().loginAs(TestUser.STANDARD);

		long millis = Duration.between(started, Instant.now()).toMillis();
		log.info("standard_user signed in after {} ms", millis);

		Assert.assertTrue(products.isLoaded(), "Standard login must always work");
	}
}
