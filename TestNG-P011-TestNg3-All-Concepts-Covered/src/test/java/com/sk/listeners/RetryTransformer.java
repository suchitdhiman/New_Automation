package com.sk.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import com.sk.annotations.Flaky;

/**
 * {@code IAnnotationTransformer} - the listener that lets you rewrite
 * {@code @Test} attributes at load time, before TestNG builds its run plan.
 *
 * <p>What it does here: any method (or class) carrying {@link Flaky} gets a
 * retry analyzer attached automatically, with the retry budget the annotation
 * asked for. Nothing else is touched.
 *
 * <p>This is the mechanism behind most "framework magic" you meet in real
 * projects - auto-tagging tests into groups by package, disabling tests whose
 * Jira ticket is still open, forcing {@code invocationCount} up in a soak run.
 * It runs once per {@code @Test} annotation, so keep it cheap and keep it
 * predictable.
 *
 * <p>Registered globally through
 * {@code META-INF/services/org.testng.ITestNGListener}, which means it applies
 * to every suite in the project without any {@code <listeners>} block.
 */
public class RetryTransformer implements IAnnotationTransformer {

	private static final Logger LOG = LogManager.getLogger(RetryTransformer.class);

	@Override
	@SuppressWarnings("rawtypes")
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
		if (testMethod == null) {
			// Class-level or constructor-level @Test - nothing to key a budget on.
			return;
		}

		Flaky flaky = testMethod.getAnnotation(Flaky.class);
		if (flaky == null) {
			flaky = testMethod.getDeclaringClass().getAnnotation(Flaky.class);
		}
		if (flaky == null) {
			return;
		}

		String key = RetryAnalyzer.budgetKey(testMethod.getDeclaringClass().getName(), testMethod.getName());
		RetryAnalyzer.registerBudget(key, flaky.maxRetries());
		annotation.setRetryAnalyzer(RetryAnalyzer.class);

		LOG.info("@Flaky detected on {} - retry analyzer attached (maxRetries={}, reason: {})",
				key, flaky.maxRetries(), flaky.reason());
	}
}
