package com.sk.tests.listeners;

import java.util.Set;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.sk.core.BaseTest;
import com.sk.core.Groups;
import com.sk.listeners.AuditListener;

/**
 * CONCEPT 7 - the complete listener picture.
 *
 * <p>Every listener interface this project implements, what it is for, and where
 * it is registered:
 *
 * <table border="1">
 *   <caption>Listeners in this framework</caption>
 *   <tr><th>Interface</th><th>Fires</th><th>Class</th><th>Registered via</th></tr>
 *   <tr><td>{@code IExecutionListener}</td><td>Once per JVM run, outside everything</td>
 *       <td>{@code ExecutionLifecycleListener}</td><td>service loader</td></tr>
 *   <tr><td>{@code IAnnotationTransformer}</td><td>While reading each {@code @Test}, before the run plan exists</td>
 *       <td>{@code RetryTransformer}</td><td>service loader</td></tr>
 *   <tr><td>{@code ISuiteListener}</td><td>Around each {@code <suite>}</td>
 *       <td>{@code SuiteExecutionListener}</td><td>service loader</td></tr>
 *   <tr><td>{@code ITestListener}</td><td>Around each {@code <test>} and each test method result</td>
 *       <td>{@code TestExecutionListener}</td><td>service loader</td></tr>
 *   <tr><td>{@code IInvokedMethodListener}</td><td>Around EVERY method, configuration ones included</td>
 *       <td>{@code MethodInvocationListener}</td><td>service loader</td></tr>
 *   <tr><td>{@code IConfigurationListener}</td><td>Results of {@code @Before*} / {@code @After*}</td>
 *       <td>{@code ConfigurationLogListener}</td><td>service loader</td></tr>
 *   <tr><td>{@code IMethodInterceptor}</td><td>Once, to reorder or drop the run plan</td>
 *       <td>{@code SmokeFirstInterceptor}</td><td>{@code <listeners>} in one suite XML</td></tr>
 *   <tr><td>{@code IRetryAnalyzer}</td><td>After a failure, to decide on a re-run</td>
 *       <td>{@code RetryAnalyzer}</td><td>{@code @Test} attribute, or the transformer</td></tr>
 *   <tr><td>{@code IReporter}</td><td>Once, after every suite has finished</td>
 *       <td>{@code ExecutionSummaryReporter}</td><td>service loader</td></tr>
 *   <tr><td>{@code ITestListener} again</td><td>Only the {@code <test>} block holding this class</td>
 *       <td>{@code AuditListener}</td><td>{@code @Listeners} below</td></tr>
 * </table>
 *
 * <h2>Firing order around one test method</h2>
 * <pre>
 *   IExecutionListener.onExecutionStart
 *     IAnnotationTransformer.transform            (per @Test, at load time)
 *       ISuiteListener.onStart
 *         IMethodInterceptor.intercept
 *           ITestListener.onStart                 (per &lt;test&gt; tag)
 *             IInvokedMethodListener.beforeInvocation   (@BeforeMethod)
 *             IInvokedMethodListener.beforeInvocation   (the @Test)
 *               ... the test method body ...
 *             IInvokedMethodListener.afterInvocation    &lt;- screenshot taken here
 *             ITestListener.onTestSuccess / Failure / Skipped
 *             IRetryAnalyzer.retry                      (only after a failure)
 *           ITestListener.onFinish
 *       ISuiteListener.onFinish
 *   IReporter.generateReport
 *   IExecutionListener.onExecutionFinish
 * </pre>
 *
 * <p>Browser-free: this class is about the framework, not the application.
 */
@Listeners(AuditListener.class)
public class ListenerShowcaseTest extends BaseTest {

	@Test(priority = 1, groups = Groups.CONCEPT,
			description = "The @Listeners listener is wired up and sees this class")
	public void annotatedListenerObservesThisClass() {
		Assert.assertTrue(AuditListener.observedCount() >= 1,
				"@Listeners should have registered the audit listener, but it saw nothing.");
		Assert.assertTrue(AuditListener.observedClasses().contains(getClass().getSimpleName()),
				"The audit listener should at least have seen the class that declares it. Saw: "
						+ AuditListener.observedClasses());
	}

	@Test(priority = 2, groups = Groups.CONCEPT,
			description = "It keeps counting for every method in this class")
	public void annotatedListenerKeepsCounting() {
		Assert.assertTrue(AuditListener.observedCount() >= 2,
				"Every method in this class should be observed. Count: " + AuditListener.observedCount());
	}

	/**
	 * The scoping lesson. {@code @Listeners} does <b>not</b> limit the listener to
	 * the annotated class - TestNG attaches it to the {@code TestRunner} for the
	 * whole {@code <test>} block. Run this from {@code testng.xml}, where this
	 * class shares a block with four others, and the log shows the audit listener
	 * observing all five.
	 *
	 * <p>It only logs, because whether other classes are present depends on which
	 * suite you launched: {@code suites/07-listeners.xml} runs this class on its
	 * own, {@code testng.xml} does not. Asserting on that would make the test a
	 * statement about the XML rather than about TestNG.
	 */
	@Test(priority = 3, groups = Groups.CONCEPT,
			description = "@Listeners scopes to the whole <test> block, not to the annotated class")
	public void annotatedListenerScopesToTheWholeTestBlock(ITestContext context) {
		Set<String> observed = AuditListener.observedClasses();

		log.info("Audit listener has observed {} test start(s) across {}: {}",
				AuditListener.observedCount(), observed.size(), observed);
		log.info("Classes in this <test> block [{}]: {}", context.getName(),
				context.getCurrentXmlTest().getXmlClasses().size());

		if (observed.size() > 1) {
			log.info("Confirmed: @Listeners reached {} classes, not just the one that declares it.", observed.size());
		} else {
			log.info("Only this class is in the <test> block, so there was nothing else to reach. "
					+ "Run testng.xml to see the wider scope.");
		}

		Assert.assertTrue(observed.contains(getClass().getSimpleName()),
				"The declaring class must always be observed. Saw: " + observed);
	}

	@Test(priority = 4, groups = Groups.CONCEPT,
			description = "The globally registered listeners are the ones writing the reports")
	public void globalListenersAreActive(ITestContext context) {
		// Proof that TestExecutionListener is wired up: it is what populates the
		// context counters this assertion reads.
		Assert.assertNotNull(context.getPassedTests(), "Test context should be tracking results");
		Assert.assertTrue(context.getPassedTests().size() >= 3,
				"The three earlier methods in this class should already be recorded as passed. "
						+ "If this is 0, no ITestListener is registered - check "
						+ "src/test/resources/META-INF/services/org.testng.ITestNGListener");
	}
}
