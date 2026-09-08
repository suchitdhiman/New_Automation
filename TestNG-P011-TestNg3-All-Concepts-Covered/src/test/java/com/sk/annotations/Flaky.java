package com.sk.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test that is known to be unstable for a reason outside our control -
 * a third party sandbox, a slow environment, an animation that occasionally
 * loses a click.
 *
 * <p>{@code RetryTransformer} reads this at annotation-transform time and wires
 * a retry analyzer onto the method. That is better than
 * {@code @Test(retryAnalyzer = RetryAnalyzer.class)} on eighty methods, and far
 * better than retrying <em>everything</em>: a blanket retry policy hides real
 * regressions behind a green tick and triples the runtime of a genuinely
 * broken suite.
 *
 * <p>Treat every use of this annotation as technical debt with a ticket number
 * in {@link #reason()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Flaky {

	/** How many extra attempts after the first failure. */
	int maxRetries() default 2;

	/** Why this is flaky and what needs to happen for the annotation to go away. */
	String reason() default "unspecified";
}
