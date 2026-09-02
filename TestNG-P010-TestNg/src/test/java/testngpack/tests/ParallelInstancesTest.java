package testngpack.tests;

import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import testngpack.base.BaseClass;
import testngpack.utils.Log;

/**
 * Target for {@code parallel="instances"}.
 *
 * <p>The @Factory produces three instances of this class, each pinned to a
 * different url key. With parallel="instances" TestNG runs one thread per
 * instance (all methods of an instance stay on the same thread).
 */
public class ParallelInstancesTest extends BaseClass {

	private final String urlKey;

	/** TestNG needs a no-arg constructor for the plain (non-factory) run. */
	public ParallelInstancesTest() {
		this("amazon");
	}

	public ParallelInstancesTest(String urlKey) {
		this.urlKey = urlKey;
	}

	@Factory
	public static Object[] createInstances() {
		return new Object[] {
				new ParallelInstancesTest("amazon"),
				new ParallelInstancesTest("bbc"),
				new ParallelInstancesTest("facebook"),
		};
	}

	@Test
	public void siteIsReachable() {
		openUrl(urlKey);
		Log.info("instance[" + urlKey + "] on thread " + Thread.currentThread().getName());
		Assert.assertTrue(driver().getCurrentUrl().startsWith("http"), urlKey + " should be reachable");
	}

	@Test(dependsOnMethods = "siteIsReachable")
	public void siteHasTitle() {
		// A fresh @BeforeMethod means a fresh browser, so navigate again.
		openUrl(urlKey);
		Assert.assertFalse(getTitle().trim().isEmpty(), urlKey + " should expose a page title");
	}

	@Override
	public String toString() {
		return "ParallelInstancesTest(" + urlKey + ")";
	}
}
