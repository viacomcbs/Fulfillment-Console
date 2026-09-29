# Fulfillment Console — Email Report Kit (extract only)

Synergy Java + TestNG **email report** sources extracted from Fulfillment Console.  
Does **not** include page objects, test cases, or unrelated automation.

**Generated:** 2026-09-25  
**Contact:** Akilandeswari Sundararajan

---

## What this zip contains

| Area | Purpose |
|------|---------|
| `java/.../common/util/EmailUtil.java` | Main email sender (HTML table + PDF attachment) |
| `java/.../common/util/reporting/*` | PDF generator, Allure reader, consolidated builders, failure categories |
| `java/.../helpers/leftfilters/LeftFilterEmailReport.java` | Scenario-wise pass/fail HTML table |
| `java/.../helpers/leftfilters/LeftFilterFailureAnalyzer.java` | Failure → Next Action (Synergy / defect / code) |
| `java/.../common/listeners/SuiteListeners.java` | Auto-email after each suite + daily aggregate copy |
| `java/.../tests/*CombinedEmailReportTest.java` | End-of-run consolidated email harness |
| `scripts/send-daily-consolidated-email.ps1` | End-of-day merged email |
| `scripts/start-daily-email-session.ps1` | Per-run email + aggregate folder setup |
| `suites/*CombinedEmail*.xml` | Example TestNG suites for consolidated send |

---

## Features (current)

- **Per-run email** — stakeholder (passed-only) + internal (full status)
- **Scenario table** in HTML email (#, Test case ID, Scenario, Status)
- **PDF attachment** with execution summary
- **Consolidated email** — merge multiple session `testng-results.xml` files
- **Daily workflow** — save each run → one end-of-day email
- **Dedupe** — same test case re-run → keep **PASS**
- **Synergy filter** — exclude UnknownHost / session-lost failures from consolidated report

---

## External dependencies (not in this zip)

Your target project must already provide:

- `Config.java` — suite parameter reader (`SendReportEmailAddress`, etc.)
- `Logger.java`, `TestUtil.java`, `SoftAssert` / `Verify` (as used by analyzers)
- Synergy: `com.synergy.core.reporting.Emailer`, `AllureReportGenerator`
- TestNG listeners registration (see `config/`)

---

## Suite parameters

```xml
<parameter name="SendReportAutoEmails" value="true"/>
<parameter name="SendReportEmailAddress" value="team@paramount.com"/>
<parameter name="SendReportInternalEmailAddress" value="qa-lead@paramount.com"/>
<parameter name="LeftFilterEmailSuiteTitle" value="My Suite Title"/>
<parameter name="AppendToDailyAggregate" value="true"/>
<parameter name="DailyAggregateDir" value="test-output/daily-aggregate/2026-09-25"/>
<parameter name="ConsolidatedEmailDedupePreferPass" value="true"/>
<parameter name="ConsolidatedEmailExcludeSynergyIssues" value="true"/>
```

---

## PDF tester name placeholder

In `ExecutionReportPdfGenerator.java` / `ExecutionReportEmailBuilder.java`, set tester display to your app QA names, e.g.:

```java
private String testerName = "{{Tester1, Tester2, Tester3}}";
```

Or pass via builder: `.testerName("{{Tester1, Tester2, Tester3}}")`.

---

## Quick start (Fulfillment Console repo)

**Each run (IntelliJ VM options):**

```
-Dsystem.test.appendtodailyaggregate=true
-Dsystem.test.dailyaggregatedir=test-output/daily-aggregate/2026-09-25
-Dsystem.test.sendreportautoemails=true
```

**End of day:**

```powershell
.\scripts\send-daily-consolidated-email.ps1
```

---

## File manifest

Run from kit root:

```powershell
Get-ChildItem -Recurse -File | Select-Object FullName
```
