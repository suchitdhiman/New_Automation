package com.sk.automation.base;

import com.sk.automation.config.ConfigManager;
import com.sk.automation.driver.Browser;
import com.sk.automation.driver.DriverFactory;
import com.sk.automation.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;

/**
 * Lifecycle every test class inherits: one fresh browser per test method, always
 * closed afterwards.
 *
 * <p>A per-method driver costs a few seconds of launch time and buys independence —
 * no test inherits cookies, storage or a stray dialog from the one before it, and
 * any test can be run on its own to reproduce a failure.
 *
 * <p>{@code alwaysRun = true} on the teardown is not decoration: without it a failure
 * in setup skips the teardown and leaks a browser process.
 */
public abstract class BaseTest {

    private static final Logger LOG = LogManager.getLogger(BaseTest.class);

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional String browserParameter, Method method) {
        // Precedence: -Dbrowser  >  testng.xml parameter  >  config.properties
        String requested = System.getProperty("browser",
                browserParameter != null ? browserParameter : ConfigManager.get("browser"));

        LOG.info("Preparing '{}' on {}", method.getName(), requested);
        DriverManager.set(DriverFactory.create(Browser.from(requested)));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method method) {
        LOG.info("Tearing down '{}'", method.getName());
        DriverManager.quit();
    }
}
