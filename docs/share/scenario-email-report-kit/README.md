# Scenario-Wise TestNG Email Report Kit

Synergy Java + TestNG automation — adds a **scenario table** to execution emails with **Next Action** on failures.

**Source:** Fulfillment Console automation (`LeftFilterEmailReport` pattern)  
**Use for:** Any Synergy TestNG project (Admin Console, MFP, etc.)

---

## Email table format

| # | Manual ID | Scenario | Status | Next Action |
|---|-----------|----------|--------|-------------|
| 1 | TC_001 | Login — valid user | PASS | |
| 2 | TC_002 | Filter — table sync | FAIL | Raise defect |

**Next Action** (failures only):
- **Synergy Issue** — browser session / login / connectivity
- **Raise defect** — application or data mismatch
- **Code Issue** — locator / automation / timeout

---

## Files in this kit

| File | Copy to your project |
|------|----------------------|
| `java/LeftFilterEmailReport.java` | `src/test/java/.../helpers/email/LeftFilterEmailReport.java` |
| `java/LeftFilterFailureAnalyzer.java` | same package folder |
| `java/LeftFilterEmailScenario.java` | same package folder |
| `java/ScenarioEmailSuiteListener.java` | `src/test/java/.../listeners/ScenarioEmailSuiteListener.java` |
| `java/EmailUtil-integration-snippet.txt` | Merge into your existing `EmailUtil.java` |
| `java/SuiteListeners-integration-snippet.txt` | Merge into your existing `SuiteListeners.java` |
| `config/example-suite.xml` | Reference for suite parameters |
| `config/META-INF/services/org.testng.ITestNGListener` | Auto-register listeners (IntelliJ + Maven) |
| `config/pom-surefire-snippet.xml` | Maven surefire listener config |

---

## Step-by-step incorporation

### 1. Copy Java files

1. Create package e.g. `com.yourteam.test.common.email` (or keep FC package if same repo).
2. Copy the 3 report classes + `ScenarioEmailSuiteListener.java`.
3. Update `package` lines and imports to match your project.

### 2. Adapt dependencies in `LeftFilterEmailReport.java`

This kit references Fulfillment Console utilities. Replace with your equivalents:

| FC class | Your project |
|----------|--------------|
| `Config.getString("TestEnvironment")` | Your config / suite parameter reader |
| `Logger.logReportMessage(...)` | Your logger |
| `AllureAttachment.captureSessionIdFromDriver()` | Your Synergy session ID capture (optional — can return `""`) |
| `AllureAttachment.buildRecordingUrl(sessionId)` | Your recording URL builder (optional) |

If you do not have session recording, stub both Allure methods to return empty string.

### 3. Wire `SuiteListeners` (global — any test run)

In your existing `SuiteListeners.onStart(ISuite suite)`:

```java
LeftFilterEmailReport.configureForSuite(suite);
```

Add `ITestListener` methods:

```java
@Override
public void onTestSuccess(ITestResult result) {
    LeftFilterEmailReport.recordResult(result);
}
@Override
public void onTestFailure(ITestResult result) {
    LeftFilterEmailReport.recordResult(result);
}
@Override
public void onTestSkipped(ITestResult result) {
    LeftFilterEmailReport.recordResult(result);
}
```

See `java/SuiteListeners-integration-snippet.txt` for full example.

### 4. Wire `EmailUtil` (inject HTML into email body)

In `sendResultEmail(...)`:

```java
} else if (LeftFilterEmailReport.isEnabled()) {
    summaryPass = LeftFilterEmailReport.getPassedCount();
    summaryFail = LeftFilterEmailReport.getFailedCount();
    summarySkip = LeftFilterEmailReport.getSkippedCount();
}
// ...
} else if (LeftFilterEmailReport.isEnabled()) {
    scenarioSection = LeftFilterEmailReport.buildHtmlSection();
}
```

See `java/EmailUtil-integration-snippet.txt`.

### 5. Register listeners globally

**Option A — META-INF (recommended for IntelliJ):**  
Copy `config/META-INF/services/org.testng.ITestNGListener` to  
`src/test/resources/META-INF/services/org.testng.ITestNGListener`  
Update class names to your package.

**Option B — Maven pom.xml:**

```xml
<property>
  <name>listener</name>
  <value>your.pkg.AllureListeners,your.pkg.SuiteListeners,your.pkg.ScenarioEmailSuiteListener</value>
</property>
```

**Option C — Suite XML:**

```xml
<listeners>
  <listener class-name="your.pkg.SuiteListeners"/>
  <listener class-name="your.pkg.ScenarioEmailSuiteListener"/>
</listeners>
```

### 6. Suite XML parameters (required for email)

```xml
<parameter name="SendReportAutoEmails" value="true"/>
<parameter name="SendReportEmailAddress" value="team.member@paramount.com"/>

<!-- Optional title in email header -->
<parameter name="LeftFilterEmailSuiteTitle" value="My App — Regression Suite"/>

<!-- Opt OUT of scenario table for a specific suite -->
<!-- <parameter name="LeftFilterEmailReport" value="false"/> -->
```

**Note:** Scenario report is **ON by default** for every suite. Set `LeftFilterEmailReport=false` to disable.

---

## Test class annotation (optional)

```java
@LeftFilterEmailScenario(
    manualId = "TC_001",
    scenario = "Login — valid service account"
)
public class Login_ValidUserTest extends BaseTest {
    @Test
    public void loginValidUser() { ... }
}
```

Without annotation, **Manual ID** = test class name, **Scenario** = humanized class name.

For left-filter style names `LF_O_TC120_ActivityType_BasicTest`, IDs and scenarios are parsed automatically.

---

## Verify it works

1. Run any suite with `SendReportAutoEmails=true`.
2. Log should show: `Scenario email report enabled for suite: ...`
3. Email body includes HTML table with columns: #, Manual ID, Scenario, Status, Next Action.

---

## Contact

Fulfillment Console QA — Akilandeswari Sundararajan  
Reference implementation: `Fulfillment-Console` repo, branch with global `SuiteListeners` wiring.
