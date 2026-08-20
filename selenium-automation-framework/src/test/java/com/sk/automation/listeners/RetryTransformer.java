package com.sk.automation.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Applies {@link RetryAnalyzer} to every test automatically.
 *
 * <p>Saves annotating each method with {@code @Test(retryAnalyzer = ...)} and, more
 * importantly, means a new test cannot be added without the policy.
 */
public class RetryTransformer implements IAnnotationTransformer {

	@Override
	@SuppressWarnings("rawtypes")
	public void transform(ITestAnnotation annotation, Class testClass,
	                      Constructor testConstructor, Method testMethod) {
        if (annotation.getRetryAnalyzerClass() == null
                || !RetryAnalyzer.class.equals(annotation.getRetryAnalyzerClass())) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
