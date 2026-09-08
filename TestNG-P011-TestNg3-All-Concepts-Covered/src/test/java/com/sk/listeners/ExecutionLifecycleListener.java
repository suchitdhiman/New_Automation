package com.sk.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IExecutionListener;

import com.sk.core.ConfigManager;

/**
 * {@code IExecutionListener} - the outermost hook TestNG offers. Fires once per
 * JVM run, before the first suite starts and after the last one ends, even when
 * the run contains several suites via {@code <suite-files>}.
 *
 * <p>Use it for things that must happen exactly once no matter how the run is
 * composed: creating output folders, resetting the ledger, starting or stopping
 * an Appium server, opening a database connection pool.
 *
 * <p>Do not use {@code @BeforeSuite} for these - a suite-of-suites run would
 * execute it once per child suite.
 */
public class ExecutionLifecycleListener implements IExecutionListener {

	private static final Logger LOG = LogManager.getLogger(ExecutionLifecycleListener.class);

	private long startedAt;

	@Override
	public void onExecutionStart() {
		startedAt = System.currentTimeMillis();

		// A stale ledger from a previous run inside the same JVM would double count.
		ExecutionLedger.clear();
		RetryAnalyzer.reset();

		createDirectory(ConfigManager.get("screenshot.dir"));
		createDirectory(ConfigManager.get("custom.report.dir"));

		LOG.info("TestNG execution starting | Java {} | OS {}",
				System.getProperty("java.version"), System.getProperty("os.name"));
	}

	@Override
	public void onExecutionFinish() {
		long seconds = (System.currentTimeMillis() - startedAt) / 1000;
		LOG.info("TestNG execution finished in {}s | {} test result(s) recorded", seconds, ExecutionLedger.total());
	}

	private void createDirectory(String path) {
		try {
			Files.createDirectories(Paths.get(path));
		} catch (IOException e) {
			LOG.warn("Could not create output directory [{}]: {}", path, e.getMessage());
		}
	}
}
