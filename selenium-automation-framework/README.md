# Selenium Automation Framework

Page Object based UI automation on **Selenium 4 + TestNG + Java 17**, structured for parallel execution and CI.

---

## Project layout

```
src/main/java/com/sk/automation/
├── config/       ConfigManager          layered configuration
├── driver/       Browser                supported browsers as an enum
│                 DriverFactory          builds a configured WebDriver
│                 DriverManager          ThreadLocal driver holder
├── exceptions/   FrameworkException     harness defects, not test failures
├── pages/        BasePage               waits, logging, shared interactions
│                 JavaApiDocsPage        framed docs site
│                 NestedFramesPage       nested frame demo
└── utils/        ScreenshotUtil         capture once, serve to disk + report
                  ElementHighlighter     optional visual debugging

src/main/resources/
├── config/       config.properties      base values
│                 config-qa.properties   environment overlays
│                 config-ci.properties
└── log4j2.xml

src/test/java/com/sk/automation/
├── base/         BaseTest               per-method driver lifecycle
├── listeners/    TestListener           reporting hooked into TestNG
│                 ExtentReportManager    thread-safe Extent instance
│                 RetryAnalyzer          bounded retries
│                 RetryTransformer       applies retries to every test
└── tests/        SwitchFramesTest

src/test/resources/suites/
├── regression.xml
└── smoke.xml
```

---

## Running

```bash
mvn clean test                                        # default: chrome, qa
mvn clean test -Dbrowser=edge                         # override browser
mvn clean test -Dheadless=true -Denv=ci               # CI profile
mvn clean test -Dsuite.file=src/test/resources/suites/smoke.xml
mvn clean test -Dthread.count=5                       # widen parallelism
```

Output lands under `target/`: `reports/` (HTML), `screenshots/` (failures only), `logs/`.

Configuration resolves in this order, first match wins:
**`-D` system property → environment variable → `config-<env>.properties` → `config.properties`**

---

## What changed, and why

| Original | Now | Reason |
|---|---|---|
| `static WebDriver` on `BaseClass` | `ThreadLocal<WebDriver>` in `DriverManager` | Shared static state makes parallel execution impossible — two tests fight over one window. This single change is what unlocks `parallel="methods"`. |
| `main()` method as the runner | TestNG with suites, groups, listeners | No pass/fail semantics existed. The script could only fail on an exception, so a page loading the wrong content counted as a pass. |
| No assertions | AssertJ assertions on outcomes | A test that asserts nothing tests nothing. |
| `defaultPath + "\\src\\test\\resources\\..."` | Classpath resource loading | Backslashes break on Linux and in containers. The same JAR now runs anywhere. |
| Locators in `or.properties` keyed by suffix (`_xPath`, `_parialLinkText`) | Typed `By` constants inside page objects | Suffix parsing has no compile-time safety — the typo `_parialLinkText` silently returns `null` and surfaces as an unrelated NPE later. |
| `Thread.sleep(2000)` between frames | `frameToBeAvailableAndSwitchToIt` | Fixed sleeps are simultaneously too slow locally and too short on a loaded CI agent. |
| Implicit + explicit waits together | Implicit pinned to `Duration.ZERO` | Mixing them compounds timeouts unpredictably — a leading cause of flaky suites. |
| `if (!isElementPresent(...)) { print }` then continue | Wait, then act; failure propagates | Printing "Element is not present" and proceeding just moved the real error somewhere less informative. |
| `browser.equalsIgnoreCase("firfox")` | `Browser` enum with validation | The typo meant Firefox silently fell through to `throw new Exception("browser not available")`. |
| `ExtentHtmlReporter` | `ExtentSparkReporter` | The former was removed in ExtentReports 5. |
| Log4j 1.x | Log4j 2.x, with Selenium's SLF4J bridged in | 1.x is end of life and gets no security fixes. |
| `e.printStackTrace()` | Structured logging | Stack traces on stdout are invisible in CI and unsearchable. |
| Screenshot method `private`, never called | `TestListener.onTestFailure` | Dead code. Screenshots now attach automatically, Base64-embedded so the report survives being emailed. |
| `webDriver.quit()` in `finally` | Null-safe quit with `ThreadLocal.remove()` | If the launch failed, the original threw NPE and masked the real cause. Not calling `remove()` leaks browser processes across a long run. |
| Duplicate `selectOption` / `typeText` | One `type(By, String)` | Both called `sendKeys`. |

---

## Design notes

**Page objects expose intent, not mechanics.** A test says `selectPackage("java.applet")`; it never learns that a frame called `packageListFrame` exists. When the site restructures, one class changes.

**One browser per test method.** Costs a few seconds of launch time, buys independence: no test inherits cookies, storage or a stray dialog, and any test can be run alone to reproduce a failure.

**Retries are bounded and logged.** `retry.count` defaults to 1. Retries absorb environment noise but hide genuine intermittent bugs just as easily, so every retry is written to the log — a test that only passes on the second attempt stays visible rather than quietly green.

**`alwaysRun = true` on teardown** is not decoration. Without it, a failure in setup skips teardown and leaks a browser process.

---

## Two things to check before your first run

1. **Dependency versions** in `pom.xml` are current as of early 2026. Run `mvn versions:display-dependency-updates` and bump before you commit.
2. **`url.javadocs`** points at Oracle's Java 8 API docs. Frames were removed from JavaDoc in JDK 11, and Oracle has been revising the legacy Java 8 pages — if the frameset is gone, those two tests will fail on a locator, not on your code. `NestedFramesPage` runs against a stable demo site and is the reliable frame coverage; keep it as the smoke test.

## Possible next steps

- Swap Extent for **Allure** if you want history and trend graphs across builds
- **Selenium Grid** or Testcontainers for cross-browser matrix runs
- **Spotless + Checkstyle** in the build to enforce formatting automatically
- A `@DataProvider` layer if scenarios start repeating with different inputs
