package com.sk.listeners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.sk.core.ConfigManager;
import com.sk.listeners.ExecutionLedger.Status;
import com.sk.listeners.ExecutionLedger.TestOutcome;
import com.sk.utils.ExcelWriter;

/**
 * {@code IReporter} - runs once, after every suite has finished, with the whole
 * run available to it. The right place to produce anything that summarises the
 * <em>run</em> rather than a single test.
 *
 * <p>Three outputs:
 * <ol>
 *   <li>a console block, because that is what you actually look at;</li>
 *   <li>{@code test-output/custom-report/execution-summary.html}, self-contained
 *       so it can be attached to a Jenkins build or emailed;</li>
 *   <li>{@code test-output/excel/...-Results.xlsx}, which is the only artefact
 *       a non-technical stakeholder will open.</li>
 * </ol>
 *
 * <p>Everything is built from {@link ExecutionLedger}, not from
 * {@code ISuite.getResults()}. The ledger has already filtered out retry
 * attempts, so the totals match what a human would count.
 */
public class ExecutionSummaryReporter implements IReporter {

	private static final Logger LOG = LogManager.getLogger(ExecutionSummaryReporter.class);
	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		List<TestOutcome> outcomes = ExecutionLedger.all();
		if (outcomes.isEmpty()) {
			LOG.warn("Nothing was recorded - no summary produced. Is TestExecutionListener registered?");
			return;
		}

		printConsoleSummary(outcomes);
		writeHtmlSummary(outcomes);

		if (ConfigManager.getBoolean("excel.write.results")) {
			ExcelWriter.writeResults(ConfigManager.get("excel.results.path"), outcomes);
		}
	}

	// -- console -------------------------------------------------------------

	private void printConsoleSummary(List<TestOutcome> outcomes) {
		long passed = ExecutionLedger.count(Status.PASSED);
		long failed = ExecutionLedger.count(Status.FAILED);
		long skipped = ExecutionLedger.count(Status.SKIPPED);
		int total = outcomes.size();

		StringBuilder out = new StringBuilder(System.lineSeparator());
		out.append("+--------------------------------------------------------------------------+").append(nl());
		out.append(row("EXECUTION SUMMARY")).append(nl());
		out.append("+--------------------------------------------------------------------------+").append(nl());
		out.append(row("Environment   : " + ConfigManager.activeEnvironment() + "  (" + ConfigManager.baseUrl() + ")")).append(nl());
		out.append(row("Finished at   : " + LocalDateTime.now().format(TIMESTAMP))).append(nl());
		out.append(row("Total tests   : " + total)).append(nl());
		out.append(row("PASSED        : " + passed + percent(passed, total))).append(nl());
		out.append(row("FAILED        : " + failed + percent(failed, total))).append(nl());
		out.append(row("SKIPPED       : " + skipped + percent(skipped, total))).append(nl());
		out.append(row("Total runtime : " + (ExecutionLedger.totalDurationMs() / 1000) + "s of test-method time")).append(nl());
		out.append("+--------------------------------------------------------------------------+").append(nl());

		if (failed > 0 || skipped > 0) {
			out.append(nl()).append("  Not-passed detail:").append(nl());
			outcomes.stream()
					.filter(outcome -> outcome.status() != Status.PASSED)
					.forEach(outcome -> out.append("   [").append(outcome.status()).append("] ")
							.append(outcome.displayName())
							.append(outcome.failureReason().isBlank() ? "" : "  ->  " + outcome.failureReason())
							.append(nl()));
		}

		// Deliberately System.out and not the logger: this block is the thing a
		// human reads at the end of a run and it should not carry a log prefix.
		System.out.println(out);
	}

	private String row(String text) {
		return String.format("| %-72s |", text);
	}

	private String percent(long count, int total) {
		return total == 0 ? "" : String.format("  (%.1f%%)", (count * 100.0) / total);
	}

	private String nl() {
		return System.lineSeparator();
	}

	// -- html ----------------------------------------------------------------

	private void writeHtmlSummary(List<TestOutcome> outcomes) {
		long passed = ExecutionLedger.count(Status.PASSED);
		long failed = ExecutionLedger.count(Status.FAILED);
		long skipped = ExecutionLedger.count(Status.SKIPPED);

		StringBuilder html = new StringBuilder();
		html.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">");
		html.append("<title>TestNG Execution Summary</title><style>");
		html.append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;background:#f6f7f9;color:#20232a}");
		html.append("h1{font-size:20px;margin:0 0 4px}h2{font-size:15px;margin:28px 0 8px}");
		html.append(".meta{color:#5b6470;font-size:13px;margin-bottom:18px}");
		html.append(".cards{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:22px}");
		html.append(".card{flex:1 1 150px;background:#fff;border:1px solid #e2e5ea;border-radius:8px;padding:14px 16px}");
		html.append(".card .n{font-size:26px;font-weight:600}.card .l{font-size:12px;color:#5b6470;text-transform:uppercase;letter-spacing:.5px}");
		html.append(".pass{color:#1a7f37}.fail{color:#c1121f}.skip{color:#9a6700}");
		html.append("table{border-collapse:collapse;width:100%;background:#fff;font-size:13px}");
		html.append("th,td{border:1px solid #e2e5ea;padding:7px 9px;text-align:left;vertical-align:top}");
		html.append("th{background:#eef1f5;font-weight:600}");
		html.append("tr.PASSED td:nth-child(4){color:#1a7f37;font-weight:600}");
		html.append("tr.FAILED td:nth-child(4){color:#c1121f;font-weight:600}");
		html.append("tr.FAILED{background:#fff5f5}");
		html.append("tr.SKIPPED td:nth-child(4){color:#9a6700;font-weight:600}");
		html.append("tr.SKIPPED{background:#fffbf0}");
		html.append("</style></head><body>");

		html.append("<h1>TestNG Execution Summary</h1>");
		html.append("<div class=\"meta\">")
				.append("Environment <b>").append(escape(ConfigManager.activeEnvironment())).append("</b> &middot; ")
				.append(escape(ConfigManager.baseUrl())).append(" &middot; ")
				.append(LocalDateTime.now().format(TIMESTAMP))
				.append("</div>");

		html.append("<div class=\"cards\">");
		html.append(card("Total", String.valueOf(outcomes.size()), ""));
		html.append(card("Passed", String.valueOf(passed), "pass"));
		html.append(card("Failed", String.valueOf(failed), "fail"));
		html.append(card("Skipped", String.valueOf(skipped), "skip"));
		html.append("</div>");

		html.append("<h2>All results</h2>");
		html.append("<table><thead><tr>")
				.append("<th>#</th><th>Class</th><th>Test</th><th>Status</th>")
				.append("<th>ms</th><th>Retries</th><th>Thread</th><th>Detail</th>")
				.append("</tr></thead><tbody>");

		int index = 1;
		for (TestOutcome outcome : outcomes) {
			html.append("<tr class=\"").append(outcome.status()).append("\">");
			html.append("<td>").append(index++).append("</td>");
			html.append("<td>").append(escape(outcome.className())).append("</td>");
			html.append("<td>").append(escape(outcome.methodName()));
			if (!outcome.parameters().isBlank()) {
				html.append("<br><small>").append(escape(outcome.parameters())).append("</small>");
			}
			html.append("</td>");
			html.append("<td>").append(outcome.status()).append("</td>");
			html.append("<td>").append(outcome.durationMs()).append("</td>");
			html.append("<td>").append(outcome.retryCount()).append("</td>");
			html.append("<td>").append(escape(outcome.threadName())).append("</td>");
			html.append("<td>").append(escape(outcome.failureReason()));
			if (!outcome.screenshotPath().isBlank()) {
				html.append("<br><a href=\"").append(escape(outcome.screenshotPath())).append("\">screenshot</a>");
			}
			html.append("</td></tr>");
		}

		html.append("</tbody></table></body></html>");

		Path target = Paths.get(ConfigManager.get("custom.report.dir")).resolve("execution-summary.html");
		try {
			Files.createDirectories(target.getParent());
			Files.writeString(target, html.toString(), StandardCharsets.UTF_8);
			LOG.info("HTML summary written: {}", target.toAbsolutePath());
		} catch (IOException e) {
			LOG.error("Could not write the HTML summary: {}", e.getMessage());
		}
	}

	private String card(String label, String number, String cssClass) {
		return "<div class=\"card\"><div class=\"n " + cssClass + "\">" + number + "</div>"
				+ "<div class=\"l\">" + label + "</div></div>";
	}

	/** Assertion messages contain angle brackets far more often than you would think. */
	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
