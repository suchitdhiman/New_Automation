# TestNG-P011-TestNg3 - All Concepts Covered

A working TestNG reference framework built on a real e-commerce application
(**Swag Labs**, `https://www.saucedemo.com`) - sign in, browse a catalogue, sort
it, fill a cart, check out, get an order confirmation.

Every TestNG concept below is demonstrated by code that actually runs, with its
own suite XML so you can execute one idea at a time.

Verified on: **Java 17 (compiled with JDK 21) - Maven 3.9.9 - TestNG 7.10.2 -
Selenium 4.47.0 - Apache POI 5.3.0**.
Last full run: **147 tests, 146 passed, 1 skipped (deliberate), 0 failed, 83s wall clock.**

---

## Quick start

Import into Eclipse with **File > Import > Existing Maven Projects**, or from a
terminal:

```bash
mvn test
```

That runs `testng.xml` - the framework concepts plus a smoke slice of the
application. Add `-Dheadless=true` if you would rather not watch browsers open.

Run any single concept:

```bash
mvn test -DsuiteXmlFile=suites/03-parallel-methods.xml -Dheadless=true
```

Nothing to install beyond Java and Maven. Selenium 4.6+ ships **Selenium
Manager**, which downloads the matching driver binary by itself, so there is no
WebDriverManager dependency and no `chromedriver.exe` to keep in step.

### Switches

| Switch | Effect |
|---|---|
| `-Dheadless=true` | No visible browser windows. Use this in CI and for parallel suites. |
| `-Denv=staging` | Loads `config/staging.properties` on top of `config.properties`. |
| `-Dbrowser=edge` | Forces every test onto Edge. **Overrides the suite XML** - see the gotchas below. |
| `-DsuiteXmlFile=...` | Which suite to run. Default is `testng.xml`. |

---

## Concept map

| # | Concept | Run this | Read this |
|---|---|---|---|
| 1 | Annotations, execution order, `@Test` attributes | `suites/01-annotations-and-attributes.xml` | `tests/annotations/AnnotationLifecycleTest`, `TestAttributesTest` |
| 2 | Suite properties applied to classes from the XML | `suites/02-suite-parameters.xml` | `tests/suiteconfig/SuiteParameterTest` |
| 3a | `parallel="methods"` | `suites/03-parallel-methods.xml` | `tests/parallel/ParallelMethodsTest` |
| 3b | `parallel="classes"` | `suites/03-parallel-classes.xml` | `tests/parallel/ParallelClass*Test` |
| 3c | `parallel="tests"` | `suites/03-parallel-tests.xml` | same three classes, split across blocks |
| 3d | `parallel="instances"` + `@Factory` | `suites/03-parallel-instances.xml` | `tests/parallel/ParallelInstancesTest` |
| 3e | Parallel **suites** (`suiteThreadPoolSize`) | `suites/13-suite-of-suites.xml` | the file itself |
| 4 | Groups, include/exclude, group of groups | `suites/04-groups-and-filters.xml` | `tests/groups/GroupedCatalogTest` |
| 5a | `dependsOnMethods` | `suites/05-dependency-invocation-threads.xml` | `tests/dependency/CheckoutDependencyTest` |
| 5b | `dependsOnGroups`, `alwaysRun` | same | `tests/dependency/GroupDependencyTest` |
| 5c | `invocationCount`, `threadPoolSize`, `successPercentage` | same | `tests/execution/InvocationAndThreadPoolTest` |
| 5d | **Can thread-count be negative?**, timeouts | same | `tests/execution/ThreadCountBoundaryTest` |
| 6 | Hard and soft assertions | `suites/06-assertions.xml` | `tests/assertions/HardAssertionTest`, `SoftAssertionTest` |
| 7 | Listeners - all nine interfaces | `suites/07-listeners.xml` | `tests/listeners/ListenerShowcaseTest`, package `com.sk.listeners` |
| 8 | DataProviders and retry analyzers | `suites/08-dataproviders-and-retry.xml` | `tests/dataprovider/LoginDataDrivenTest`, `tests/retry/*` |
| 9 | Excel-driven testing | `suites/09-excel-driven.xml` | `tests/dataprovider/ExcelDriven*Test`, `utils/ExcelReader` |
| 10 | **Skipped**, and PASSED / FAILED / SKIPPED reporting | `suites/10-skip-and-status-showcase.xml` | `tests/skip/*` |
| 11 | Page Object Model | `suites/11-pom-end-to-end.xml` | `tests/e2e/EndToEndPurchaseTest`, package `com.sk.pages` |
| - | Cross browser | `suites/12-cross-browser.xml` | Chrome + Edge side by side |
| - | Everything at once | `suites/master-regression.xml` | six blocks, four parallel modes |

---

## What you get after a run

| Artefact | Where |
|---|---|
| Console summary block | end of the Maven output |
| Custom HTML report | `test-output/custom-report/execution-summary.html` |
| Excel results, colour coded | `test-output/excel/SwagLabsTestData-Results.xlsx` |
| Failure screenshots | `test-output/screenshots/` |
| Log with thread names | `logs/testng-execution.log` |
| TestNG default reports | `test-output/index.html` |
| Surefire reports | `target/surefire-reports/` |

To see all three statuses at once, run the showcase suite. It is the only suite
that runs the `demo-status` group and it is **meant** to finish red:

```bash
mvn test -DsuiteXmlFile=suites/10-skip-and-status-showcase.xml -Dheadless=true
```

```
+--------------------------------------------------------------------------+
| EXECUTION SUMMARY                                                        |
+--------------------------------------------------------------------------+
| Environment   : qa  (https://www.saucedemo.com)                          |
| Total tests   : 12                                                       |
| PASSED        : 3  (25.0%)                                               |
| FAILED        : 5  (41.7%)                                               |
| SKIPPED       : 4  (33.3%)                                               |
+--------------------------------------------------------------------------+
```

---

## Layout

```
pom.xml                      dependencies, surefire, the logging version pins
testng.xml                   default suite for `mvn test`
suites/                      one XML per concept, heavily commented
logs/                        log4j2 output (thread name in every line)
test-output/                 reports, screenshots, Excel results

src/test/java/com/sk/
  core/         ConfigManager, Browser, DriverFactory, BaseTest, BaseWebTest, Groups
  pages/        BasePage + six page objects (POM)
  model/        TestUser, SortOption, LoginScenario, CheckoutScenario
  listeners/    nine listeners, the retry analyzer, the execution ledger
  dataproviders/  hard-coded and Excel-backed providers
  utils/        ExcelReader, ExcelWriter, ScreenshotUtil, ThreadAuditor
  annotations/  @Flaky
  tests/        one package per concept

src/test/resources/
  config/       config.properties + qa/staging overlays
  testdata/     SwagLabsTestData.xlsx  (LoginData, CheckoutData, ReadMe sheets)
  log4j2.xml
  META-INF/services/org.testng.ITestNGListener   global listener registration
```

---

## Design decisions worth knowing

**One driver per thread.** `DriverFactory` holds a `ThreadLocal<WebDriver>`.
That single decision is what lets any suite here switch parallel modes without
touching a page object.

**A fresh browser per test method.** Slower than sharing a session, and worth it:
a shared session carries a logged-in user and a full cart into the next test, and
then the suite only passes in the order you happened to write it. The one
exception is `CheckoutDependencyTest`, which owns its browser deliberately
because its whole point is that step 4 sees what step 2 did.

**Suite-level setup lives in `ISuiteListener`, not `@BeforeSuite`.** A
`@BeforeSuite` inherited by twenty classes is easy to misread, and it can be
skipped by a group filter. A listener cannot.

**Screenshots are taken in `IInvokedMethodListener.afterInvocation`**, which
TestNG calls while the test method has only just returned and the browser is
still open. Doing it in `ITestListener.onTestFailure` is the classic way to end
up with an empty screenshots folder.

**The reports come from a custom ledger, not from `ISuite.getResults()`.**
`ExecutionLedger` filters out retry attempts, so the totals match what a human
would count.

---

## Four traps this project hit, and how they were fixed

These were all found by running the suites, not by reading about them. Each one
is commented in the code where it was fixed.

**1. `-Dbrowser` silently beat the cross-browser suite.**
For `@Parameters` injection, TestNG gives a **system property priority over the
XML `<parameter>`**. The pom was forwarding `browser` in surefire
`<systemPropertyVariables>`, so every block of `12-cross-browser.xml` started
Chrome - including the one that says `value="edge"`. Green build, wrong coverage.
Fixed by not forwarding `browser`; `-Dbrowser=edge` still works as a deliberate
override. See the comment in `pom.xml`.

**2. A page object held in an instance field is shared state.**
TestNG creates **one instance of a test class** and runs every method on it, so
under `parallel="methods"` - and far more aggressively with a `parallel = true`
DataProvider - several threads share it. A `loginPage` field assigned in
`@BeforeMethod` was being overwritten by one thread while another was using it,
and then quit. Symptoms were `NoSuchSessionException`,
`RejectedExecutionException ... [Shutting down]` and `StaleElementReference`,
none of which point at the cause. Fixed by making `loginPage()` a method that
builds from the ThreadLocal driver. See `BaseWebTest`.

**3. React drops keystrokes under parallel load.**
Swag Labs uses controlled inputs. With three headless Chromes competing for CPU,
`sendKeys` would type `standard_user` and the field would hold `standard_use`,
producing an intermittent "credentials do not match" that looks exactly like a
product bug. `BasePage.type()` now reads the value back with
`getDomProperty("value")` and retypes if it does not match.

**4. POI and Selenium disagreed about the logging stack.**
POI 5.3.0 pulls `log4j-api` 2.23.1 and `slf4j-api` 1.7.36 transitively; Maven
resolves by "nearest wins", so those beat what `log4j-core` and Selenium need.
The result was `NoSuchFieldError: DefaultFlowMessageFactory ... INSTANCE` before
a single test ran. Fixed with a `<dependencyManagement>` block, which beats
transitive resolution at any depth.

---

## Notes on the numbers

Surefire and the custom summary sometimes disagree, and the custom one is right.
TestNG marks an attempt that a retry analyzer discarded as **SKIPPED**, so a test
that failed twice and passed on the third go shows up in surefire as
"1 passed, 2 skipped". `TestExecutionListener` checks
`ITestResult.wasRetried()` and drops those rows, which is why the summary block
adds up and surefire's does not.

## Editing the test data

`src/test/resources/testdata/SwagLabsTestData.xlsx` has three sheets. Add a row
to `LoginData` or `CheckoutData`, set **Execute** to `Y`, and it runs on the next
build - no Java changes. Set it to `N` to park a row without deleting it. The
`ReadMe` sheet documents every column.

Columns are matched **by header name, not by position**, so inserting or
reordering a column breaks nothing. Renaming one that a test requires fails
loudly, with the spreadsheet row number in the message.
