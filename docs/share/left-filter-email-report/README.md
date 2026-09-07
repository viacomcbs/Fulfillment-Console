# Left Filter Regression — Scenario Email Report Kit (Updated)

Synergy Java + TestNG automation for **Fulfillment Console left-filter regression suites**.

Replaces the old email that showed only **one summary table** (passed / failed counts) with:

1. **Scenario table** — one row per test class: Manual ID, Scenario, Status, **Next Action**
2. **Execution summary** — existing pass / skip / fail table + Synergy Allure link

**Source:** `Fulfillment-Console` repo — left-filter regression (`LF_O_*`, `LF_LI_*`, `LF_OLI_*` suites)

---

## What changed vs the old kit

| Old email | Updated email |
|-----------|---------------|
| Single table: Application, Environment, Passed, Failed | Scenario table **first**, then execution summary |
| No per-test detail | Manual ID + human-readable scenario per test class |
| Failures need manual triage | **Next Action** on failed rows: `Synergy Issue`, `Raise defect`, or `Code Issue` |
| Opt-in via `LeftFilterEmailReport=true` on left-filter suites only | **On by default globally** via `SuiteListeners`; opt out with `LeftFilterEmailReport=false` |

---

## Email layout

### Scenario table

| # | Manual ID | Scenario | Status | Next Action |
|---|-----------|----------|--------|-------------|
| 1 | LF_O_TC601 | Order Status — Table Sync | PASS | |
| 2 | LF_LI_TC401 | Line Item Status — Select All | FAIL | Raise defect |

Also includes: suite title, environment, Synergy screen recording link.

### Next Action (failures only)

| Value | When |
|-------|------|
| **Synergy Issue** | Browser session, login, grid / connectivity |
| **Raise defect** | Application or data mismatch (filter sync, sort order, counts, etc.) |
| **Code Issue** | Locator, timeout, element not found, automation bug |

---

## Files in this kit

| File | Purpose |
|------|---------|
| `LeftFilterEmailReport.java` | Builds scenario HTML table; global `configureForSuite()` |
| `LeftFilterFailureAnalyzer.java` | Classifies failures → Next Action |
| `LeftFilterEmailScenario.java` | Optional `@LeftFilterEmailScenario` annotation on test classes |
| `LeftFilterSuiteListener.java` | Shared browser session for `LF_*` suites + records results |
| `SuiteListeners.java` | **Reference** — global wiring (`configureForSuite`, `ITestListener`, email before Allure) |
| `EmailUtil.java` | **Reference** — injects scenario section into email body |
| `ScenarioEmailSuiteListener.java` | Optional standalone listener (other Synergy projects) |
| `SuiteListeners-integration-snippet.txt` | Minimal merge guide for your `SuiteListeners` |
| `EmailUtil-integration-snippet.txt` | Minimal merge guide for your `EmailUtil` |
| `config/example-suite.xml` | Left-filter regression suite parameters |
| `config/META-INF/services/org.testng.ITestNGListener` | Auto-register listeners |
| `config/pom-surefire-snippet.xml` | Maven surefire listener config |

---

## Quick incorporation (Fulfillment Console — already wired)

If your branch matches this repo, you only need:

1. Copy the 3 helper classes under `.../helpers/leftfilters/` (if not already present).
2. Ensure `SuiteListeners` calls `LeftFilterEmailReport.configureForSuite(suite)` in `onStart` and implements `ITestListener` to call `recordResult`.
3. Ensure `EmailUtil.sendResultEmail` uses `LeftFilterEmailReport.buildHtmlSection()` when enabled.
4. Suite XML parameters:

```xml
<parameter name="SendReportAutoEmails" value="true"/>
<parameter name="SendReportEmailAddress" value="your.team@paramount.com"/>
<parameter name="LeftFilterEmailSuiteTitle" value="Orders Tab — Order Status all per-filter tests (PROD)"/>
<!-- Optional: disable scenario table for this suite -->
<!-- <parameter name="LeftFilterEmailReport" value="false"/> -->
```

---

## Test class naming (auto-parsed)

No annotation required when class names follow the left-filter pattern:

| Class name | Manual ID | Scenario |
|------------|-----------|----------|
| `LF_O_TC601_OrderStatus_TableSyncTest` | `LF_O_TC601` | Order Status — Table Sync |
| `LF_LI_TC401_LineItemStatus_SelectAllTest` | `LF_LI_TC401` | Line Item Status — Select All |

Optional explicit annotation:

```java
@LeftFilterEmailScenario(
    manualId = "LF_LI_TC401",
    scenario = "Line Item Status left filter — Select All checkbox behavior"
)
public class LF_LI_TC401_LineItemStatus_SelectAllTest extends ... { }
```

---

## Verify

1. Run any left-filter regression suite with `SendReportAutoEmails=true`.
2. Console log: `Scenario email report enabled for suite: ...`
3. Email body contains the **5-column scenario table** above the execution summary.

---

## Contact

Fulfillment Console QA — Akilandeswari Sundararajan
