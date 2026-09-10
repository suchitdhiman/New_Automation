package com.sk.ecom.reports;

import com.aventstack.extentreports.ExtentTest;
import com.sk.ecom.constants.FrameworkConstants;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the Extent report straight from Cucumber's event stream.
 *
 * <p>This is deliberately a plugin rather than a set of hooks. Hooks cannot see
 * the name of the step that is running, so a hook-based report can only show
 * scenarios; subscribing to {@link TestStepStarted} gives us the real Gherkin
 * text and produces a <em>Feature &rarr; Scenario &rarr; Step</em> tree.
 *
 * <p>{@link ConcurrentEventListener} tells Cucumber the plugin is safe to call
 * from scenario threads. Cucumber delivers test-case and step events on the
 * thread that runs them, so the per-thread nodes in {@link ExtentTestManager}
 * line up correctly under parallel execution; only the feature-node map is
 * shared, and that is a {@link ConcurrentHashMap}.
 *
 * <p>Register it with {@code plugin = "com.sk.ecom.reports.ExtentCucumberPlugin"}.
 */
public class ExtentCucumberPlugin implements ConcurrentEventListener {

	/** feature file name -&gt; parent node, shared across scenario threads. */
	private final Map<String, ExtentTest> featureNodes = new ConcurrentHashMap<>();

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestRunStarted.class, this::onRunStarted);
		publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
		publisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
		publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
		publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
		publisher.registerHandlerFor(TestRunFinished.class, this::onRunFinished);
	}

	/* ------------------------------------------------------------------ */

	private void onRunStarted(TestRunStarted event) {
		// Touching the singleton here creates the report file up front, so a
		// crash mid-run still leaves a readable (if partial) artefact.
		ExtentReportManager.getInstance();
	}

	private void onTestCaseStarted(TestCaseStarted event) {
		TestCase testCase = event.getTestCase();
		ExtentTest featureNode = featureNodes.computeIfAbsent(featureName(testCase.getUri()),
				name -> ExtentReportManager.getInstance().createTest(name));

		ExtentTest scenarioNode = featureNode.createNode(testCase.getName());
		List<String> tags = testCase.getTags();
		if (!tags.isEmpty()) {
			scenarioNode.assignCategory(tags.toArray(new String[0]));
		}
		scenarioNode.assignDevice(System.getProperty("browser", "chrome"));
		ExtentTestManager.setScenario(scenarioNode);
		ScreenshotStore.clear();
	}

	private void onTestStepStarted(TestStepStarted event) {
		if (!(event.getTestStep() instanceof PickleStepTestStep pickleStep)) {
			// Before/After hooks are steps too; they get no node of their own,
			// anything they log lands on the scenario node instead.
			return;
		}
		ExtentTest scenario = ExtentTestManager.getScenario();
		if (scenario == null) {
			return;
		}
		String title = pickleStep.getStep().getKeyword().trim() + " " + pickleStep.getStep().getText();
		ExtentTestManager.setStep(scenario.createNode(title));
	}

	private void onTestStepFinished(TestStepFinished event) {
		TestStep step = event.getTestStep();
		Result result = event.getResult();

		if (step instanceof HookTestStep) {
			// Only surface hooks when they break; a green hook is noise.
			if (result.getStatus() == io.cucumber.plugin.event.Status.FAILED) {
				ExtentTestManager.logFail("Hook failed: " + describe(result));
			}
			return;
		}
		if (!(step instanceof PickleStepTestStep)) {
			return;
		}

		switch (result.getStatus()) {
			case PASSED -> ExtentTestManager.logPass("Step passed");
			case FAILED -> ExtentTestManager.logFail(describe(result));
			case SKIPPED -> ExtentTestManager.logWarning("Step skipped");
			case PENDING -> ExtentTestManager.logWarning("Step is pending implementation");
			case UNDEFINED -> ExtentTestManager.logFail("No matching step definition found");
			case AMBIGUOUS -> ExtentTestManager.logFail("Ambiguous step definition: " + describe(result));
			default -> ExtentTestManager.logWarning("Step finished with status " + result.getStatus());
		}
		ExtentTestManager.clearStep();
	}

	private void onTestCaseFinished(TestCaseFinished event) {
		ExtentTestManager.clearStep();
		Result result = event.getResult();

		if (result.getStatus() == io.cucumber.plugin.event.Status.FAILED) {
			ExtentTestManager.logFail("Scenario failed: " + describe(result));
			ExtentTestManager.attachScreenshot(ScreenshotStore.take(), "Failure screenshot");
		} else if (result.getStatus() == io.cucumber.plugin.event.Status.PASSED) {
			ExtentTestManager.logPass("Scenario passed");
			if (FrameworkConstants.screenshotOnPass()) {
				ExtentTestManager.attachScreenshot(ScreenshotStore.take(), "Final state");
			}
		} else {
			ExtentTestManager.logWarning("Scenario finished with status " + result.getStatus());
		}
		ScreenshotStore.clear();
		ExtentTestManager.unload();
	}

	private void onRunFinished(TestRunFinished event) {
		ExtentReportManager.flush();
	}

	/* ------------------------------------------------------------------ */

	private static String describe(Result result) {
		Throwable error = result.getError();
		if (error == null) {
			return String.valueOf(result.getStatus());
		}
		String message = error.getMessage();
		return error.getClass().getSimpleName() + (message == null ? "" : ": " + message);
	}

	private static String featureName(URI uri) {
		return Paths.get(uri.getPath()).getFileName().toString();
	}
}
