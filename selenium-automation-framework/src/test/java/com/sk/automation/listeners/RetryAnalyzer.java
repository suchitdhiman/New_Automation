package com.sk.automation.listeners;

import com.sk.automation.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Re-runs a failed test a bounded number of times.
 *
 * <p>Worth being honest about the trade-off: retries hide genuine intermittent bugs
 * as easily as they absorb environment noise. The limit is deliberately low and
 * configurable, and every retry is logged so a test that only ever passes on the
 * second attempt is visible rather than quietly green.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRIES = ConfigManager.getInt("retry.count");

    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < MAX_RETRIES) {
            attempt++;
            LOG.warn("Retrying '{}' — attempt {} of {}",
                    result.getMethod().getMethodName(), attempt, MAX_RETRIES);
            return true;
        }
        return false;
    }
}
