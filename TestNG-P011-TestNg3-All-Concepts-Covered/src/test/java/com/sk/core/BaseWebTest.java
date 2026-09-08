package com.sk.core;

import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.sk.pages.LoginPage;

/**
 * Browser lifecycle for every UI test.
 *
 * <p>A fresh driver per test method. It is slower than sharing one browser
 * across a class, and it is worth it: a shared session carries a logged-in
 * user, a filled cart and a sort order into the next test, and then the suite
 * only passes in the order you happened to write it.
 *
 * <h2>How the browser is chosen</h2>
 * <ol>
 *   <li>{@code -Dbrowser=edge} on the command line, if set. <b>TestNG gives a
 *       system property priority over the XML for {@code @Parameters}
 *       injection</b> - it is not the other way round, however much it looks
 *       like it should be.</li>
 *   <li>otherwise {@code <parameter name="browser" value="edge"/>} from the
 *       suite XML.</li>
 *   <li>otherwise {@code browser} in {@code config/config.properties}.</li>
 * </ol>
 *
 * <p>That first rule is worth knowing because it bites hard. The pom originally
 * forwarded {@code browser} in surefire {@code <systemPropertyVariables>}, which
 * meant every {@code <test>} block of the cross-browser suite started Chrome -
 * including the one that clearly says {@code value="edge"}. Green build,
 * completely wrong coverage. The pom no longer sets it; see the comment there.
 *
 * <p>The practical consequence: {@code -Dbrowser=firefox} forces the whole run
 * onto Firefox and overrides {@code suites/12-cross-browser.xml}. That is
 * occasionally what you want, and never what you want by accident.
 */
public abstract class BaseWebTest extends BaseTest {

	/**
	 * TestNG fills the {@code @Parameters} arguments first, then injects the
	 * built-in ones ({@link Method}, {@link ITestContext}). Keep that order or
	 * you get a confusing "cannot find parameter" at runtime.
	 */
	@Parameters({ "browser" })
	@BeforeMethod(alwaysRun = true)
	public void startBrowser(@Optional String browserFromXml, Method method, ITestContext context) {
		String requested = (browserFromXml == null || browserFromXml.isBlank())
				? ConfigManager.get("browser")
				: browserFromXml;

		Browser browser = Browser.from(requested);
		log.info("--> {}.{} | browser={} | env={}",
				method.getDeclaringClass().getSimpleName(), method.getName(), browser, ConfigManager.activeEnvironment());

		WebDriver driver = DriverFactory.init(browser);
		driver.get(ConfigManager.baseUrl());
	}

	/**
	 * {@code alwaysRun = true} matters here. Without it, a test skipped because
	 * its {@code dependsOnMethods} failed would leave the browser open forever.
	 *
	 * <p>The failure screenshot is taken earlier, in
	 * {@code MethodInvocationListener.afterInvocation}, which TestNG guarantees
	 * to call while the test method has only just returned. Taking it here works
	 * too, but then every test class has to remember to do it.
	 */
	@AfterMethod(alwaysRun = true)
	public void stopBrowser() {
		DriverFactory.quit();
	}

	protected WebDriver driver() {
		return DriverFactory.getDriver();
	}

	/**
	 * The entry point for every UI test. A <b>method</b>, not a field, and that
	 * distinction is the difference between a suite that works in parallel and
	 * one that fails in ways nobody can reproduce.
	 *
	 * <h2>Why not {@code protected LoginPage loginPage;}</h2>
	 * TestNG creates <b>one instance of a test class</b> and runs every method on
	 * it. Under {@code parallel="methods"} - and even more so with a
	 * {@code parallel = true} DataProvider, where the rows run on a second thread
	 * pool - several invocations share that single instance. An instance field
	 * assigned in {@code @BeforeMethod} is therefore shared mutable state:
	 * thread B overwrites it with its own driver while thread A is halfway
	 * through using it, and thread B then quits that driver in
	 * {@code @AfterMethod}.
	 *
	 * <p>The symptoms are these, and none of them point at the real cause:
	 * <pre>
	 *   NoSuchSessionException: Session ID is null. Using WebDriver after calling quit()?
	 *   RejectedExecutionException: ... rejected from ThreadPoolExecutor[Shutting down]
	 *   StaleElementReferenceException
	 * </pre>
	 *
	 * <p>Building the page object on demand sidesteps all of it. Page objects are
	 * cheap - a driver reference and two waits - and this one is always built
	 * from {@code DriverFactory.getDriver()}, which is ThreadLocal, so each
	 * thread necessarily gets a page bound to its own browser.
	 *
	 * <p>The rule that follows: <b>in a parallel suite, a test class must hold no
	 * mutable instance state.</b> Where state genuinely has to survive between
	 * methods, use a {@link ThreadLocal} (see {@code SoftAssertionTest}) or keep
	 * the class on one thread and say so (see {@code CheckoutDependencyTest},
	 * which owns its own driver and only runs under {@code parallel="classes"}).
	 */
	protected LoginPage loginPage() {
		return new LoginPage(driver());
	}
}
