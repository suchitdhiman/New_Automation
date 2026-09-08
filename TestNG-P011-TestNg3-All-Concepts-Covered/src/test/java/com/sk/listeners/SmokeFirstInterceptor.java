package com.sk.listeners;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import com.sk.core.Groups;

/**
 * {@code IMethodInterceptor} - lets you reorder (or drop) the run plan after
 * TestNG has worked out which methods to run but before it runs any of them.
 *
 * <p>Here it pulls {@code smoke} to the front and {@code regression} to the
 * back, so a long nightly run fails fast on the things that matter most.
 *
 * <p><b>Careful with this one.</b> It overrides {@code priority} and it fights
 * with {@code dependsOnMethods}: reordering a dependency graph is at best
 * ignored and at worst a configuration error. That is why it is registered only
 * on the groups suite - via a {@code <listeners>} block in
 * {@code suites/04-groups-and-filters.xml}, which also demonstrates the
 * third way of registering a listener - and not globally through the
 * service loader.
 */
public class SmokeFirstInterceptor implements IMethodInterceptor {

	private static final Logger LOG = LogManager.getLogger(SmokeFirstInterceptor.class);

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		List<IMethodInstance> ordered = methods.stream()
				.sorted(Comparator.comparingInt(SmokeFirstInterceptor::weightOf))
				.toList();

		LOG.info("Method interceptor reordered {} method(s) for <test> [{}]: smoke first, regression last",
				ordered.size(), context.getName());
		return ordered;
	}

	private static int weightOf(IMethodInstance instance) {
		List<String> groups = Arrays.asList(instance.getMethod().getGroups());
		if (groups.contains(Groups.SMOKE)) {
			return 0;
		}
		if (groups.contains(Groups.SANITY)) {
			return 1;
		}
		if (groups.contains(Groups.REGRESSION)) {
			return 3;
		}
		return 2;
	}
}
