package com.sk.listeners;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A listener attached with {@code @Listeners(AuditListener.class)} on a single
 * test class, purely to show what that annotation actually scopes to.
 *
 * <h2>The answer, and it surprises people</h2>
 * <b>Not</b> just the annotated class. TestNG registers the listener on the
 * {@code TestRunner} for the whole {@code <test>} block that contains that
 * class, so every other class in the same block gets audited too.
 *
 * <p>Measured on TestNG 7.10.2 with {@code testng.xml}, where
 * {@code ListenerShowcaseTest} sits in the first of two {@code <test>} blocks:
 * <pre>
 *   block 1 (contains the annotated class)   25 observations
 *     AnnotationLifecycleTest    3
 *     ListenerShowcaseTest       3
 *     SkipScenarioTest           3
 *     TestAttributesTest         8
 *     ThreadCountBoundaryTest    8
 *   block 2 (no annotated class)              0 observations
 * </pre>
 *
 * <p>So if you genuinely want per-class behaviour, the listener has to filter
 * for itself - {@code result.getTestClass().getRealClass()} is the check. Do not
 * assume the annotation did it for you: a listener that writes to a database or
 * uploads an artefact will quietly do it for every class in the block.
 *
 * <h2>Which registration to use</h2>
 * <ul>
 *   <li><b>Service loader</b> ({@code META-INF/services/org.testng.ITestNGListener})
 *       for anything cross-cutting - reporting, retries, screenshots. Applies to
 *       every suite, and nobody has to remember to wire it up.</li>
 *   <li><b>{@code <listeners>} in a suite XML</b> when a listener changes how one
 *       particular run behaves - see the method interceptor in
 *       {@code suites/04-groups-and-filters.xml}.</li>
 *   <li><b>{@code @Listeners} on a class</b> when the behaviour belongs to the
 *       area that class lives in, remembering the scope above.</li>
 * </ul>
 *
 * <p>One hard limit: {@code @Listeners} cannot register an
 * {@code IAnnotationTransformer}. Annotation transformation happens while TestNG
 * is still reading the classes, before it knows about any class-level listener -
 * so a transformer must come from the service loader or the suite XML.
 */
public class AuditListener implements ITestListener {

	private static final Logger LOG = LogManager.getLogger(AuditListener.class);

	private static final AtomicInteger OBSERVED = new AtomicInteger();
	private static final Set<String> OBSERVED_CLASSES = ConcurrentHashMap.newKeySet();

	@Override
	public void onTestStart(ITestResult result) {
		String className = result.getTestClass().getRealClass().getSimpleName();
		OBSERVED_CLASSES.add(className);

		LOG.info("[audit listener] observation #{} : {}.{}",
				OBSERVED.incrementAndGet(), className, result.getMethod().getMethodName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		LOG.debug("[audit listener] passed  : {}", result.getMethod().getMethodName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		LOG.debug("[audit listener] failed  : {}", result.getMethod().getMethodName());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		LOG.debug("[audit listener] skipped : {}", result.getMethod().getMethodName());
	}

	/** How many test starts this listener has seen. Read by the showcase test. */
	public static int observedCount() {
		return OBSERVED.get();
	}

	/** Which classes it saw - the evidence for the scoping note above. */
	public static Set<String> observedClasses() {
		return Set.copyOf(OBSERVED_CLASSES);
	}
}
