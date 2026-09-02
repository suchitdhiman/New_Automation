# TestNG-P010-TestNg

Selenium + TestNG framework demonstrating `@Parameters`, `@Optional`, and every
kind of TestNG parallelism.

## Layout

```
src/test/java/testngpack/
  base/
    ConfigReader.java     loads the .properties files once, read-only afterwards
    DriverFactory.java    ThreadLocal<WebDriver> - one browser per test thread
    ExtentManager.java    one ExtentReports, one ExtentTest per thread
    BaseClass.java        @BeforeSuite / @BeforeMethod / @AfterMethod + page helpers
  listeners/
    TestListener.java     ITestListener - logging + failure screenshots
    SuiteListener.java    ISuiteListener - flushes the html report once at the end
  dataproviders/
    SearchDataProvider.java
  tests/
    AmazonSearchTest.java         @Parameters + @Optional on a @Test
    UrlNavigationTest.java        @Optional defaults vs xml values
    ParallelMethodsTest.java      target for parallel="methods"
    ParallelInstancesTest.java    @Factory, target for parallel="instances"
    DataProviderParallelTest.java target for @DataProvider(parallel = true)
  utils/Log.java          log4j wrapper that stamps the thread name

testng.xml                master suite, parallel="tests" (chrome + edge at once)
suites/
  parameters-demo.xml       @Parameters / @Optional walkthrough (serial)
  parallel-methods.xml      parallel="methods"       thread-count=4
  parallel-classes.xml      parallel="classes"       thread-count=3
  parallel-instances.xml    parallel="instances"     thread-count=3
  parallel-dataprovider.xml data-provider-thread-count=3
  regression-all.xml        runs all of the above via <suite-files>
```

## Running

From Eclipse: right-click any `.xml` above > Run As > TestNG Suite.
Right-clicking a test class also works - the `@Optional` defaults cover every
parameter, so no xml is required.

With Maven:

```bash
mvn test
```

```bash
mvn test -DsuiteXmlFile=suites/parallel-methods.xml
```

```bash
mvn test -DsuiteXmlFile=suites/regression-all.xml -Dheadless=true
```

`-Dbrowser=edge` overrides the browser from the command line; `-Dheadless=true`
runs without a visible window (recommended when several threads run at once).

## How the parameters resolve

For any `@Parameters` value, the first match wins:

1. `<parameter>` on the `<test>`
2. `<parameter>` on the `<suite>`
3. `-Dbrowser=...` / `-Dheadless=...` on the command line (read explicitly in
   `BaseClass#setUp`)
4. `@Optional("default")` on the method argument

`BaseClass#optionalParam(ITestContext, key, fallback)` does the same thing
programmatically, for places where an annotation cannot reach - inside a
`@DataProvider`, for example.

## The parallel modes

| Suite attribute | What gets its own thread | Suite file |
| --- | --- | --- |
| `parallel="methods"` | each `@Test` method | `suites/parallel-methods.xml` |
| `parallel="classes"` | each class (its methods stay in order) | `suites/parallel-classes.xml` |
| `parallel="tests"` | each `<test>` block - used for cross-browser | `testng.xml` |
| `parallel="instances"` | each object from the `@Factory` | `suites/parallel-instances.xml` |
| `@DataProvider(parallel = true)` | each data row | `suites/parallel-dataprovider.xml` |

All of them depend on one rule: **no shared mutable state across threads.** The
WebDriver, the WebDriverWait and the ExtentTest all live in `ThreadLocal`s
(`DriverFactory`, `ExtentManager`). A `static WebDriver` field would make every
mode above fail at random.

## Reports

- `Report/htmlReport.html` - Extent report, flushed once by `SuiteListener`
- `test-output/` - TestNG's own report
- `logs/selenium-test.log` - log4j, with the thread name on every line
- `failurescreenshots/` - PNG per failed test, also embedded in the Extent report
