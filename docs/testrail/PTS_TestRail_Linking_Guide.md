# PTS Orders — TestRail manual cases + automation linking

**Jira:** BSD-29441  
**TestRail project ID (this repo):** `223`  
**TestRail URL:** https://paramount.testrail.io/index.php?/projects/overview/223
**Automation suite:** `src/test/resources/FF_PTS_BSD29441_ProdServerSuite.xml`

---

## Step 1 — Import manual test cases into TestRail

1. Open **TestRail** → project **223** (Fulfillment Console).
2. Create section path (if missing):  
   `Fulfillment Console > BSD-29441 > Orders > PTS Packaging ID`
3. **Test Cases** → **Import** → **CSV**.
4. Upload:  
   `docs/testrail/FF_PTS_Orders_ManualCases_Import.csv`
5. Map columns: Title, Section, Template, Type, Priority, References, Steps, Expected Result, Automation Type, Automation ID.
6. After import, open each case and note the **TestRail case ID** (format `C1234567`).

### Manual ↔ automation mapping (Orders)

| Manual ID | TestRail title (short) | Automation class |
|-----------|------------------------|------------------|
| FF_PTS_001 | Column available in Table View | `FF_PTS_O_001_ValidateColumnAvailableInTableView` |
| FF_PTS_002 | Column visible in grid | `FF_PTS_O_002_ValidateColumnVisibleInGrid` |
| FF_PTS_011 | Column sort | `FF_PTS_O_011_ValidateColumnSort` |
| FF_PTS_009 | Column search | `FF_PTS_O_009_ValidateColumnSearch` |
| FF_PTS_015 | Table matches Details | `FF_PTS_O_015_ValidateTableMatchesDetails` |
| FF_PTS_013 | Excel export | `FF_PTS_O_013_ValidateColumnInExcelExport` |
| FF_PTS_017 | Deselect + export excludes | `FF_PTS_O_017_ValidateColumnHiddenWhenUnselectedAndExportExcludes` |

Line Items manual cases (FF_PTS_003–010, 014, 016, 018) are in `docs/BSD-29441_PTS_Packaging_ID_TestCases.md` — import separately when LI automation is ready.

---

## Step 2 — Link automation in TestRail (UI)

For each imported case:

1. Open the test case → **Automation** tab (or **References / Automation ID** fields).
2. Set **Automation Type:** `Java TestNG` (or **Other** if your project uses that).
3. Set **Automation ID** to the fully qualified test (already in the CSV), e.g.:  
   `com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_009_ValidateColumnSearch#validateColumnSearch`
4. Save.

TestRail will now show which manual case maps to which automated test.

---

## Step 3 — Link automation in code (optional — auto post results)

This project has **Synergy TestRail integration scaffolded but disabled** (`TestRailUtil.java` and `TestListeners` calls are commented out).

When you have TestRail case IDs (`C#####`), add to each `@Test` method:

```java
import io.qameta.allure.TmsLink;

@Test
@TmsLink("C1234567")   // replace with real TestRail case ID after import
@Description("FF_PTS_O_009 — PTS Packaging ID column search on PTS orders")
public void validateColumnSearch() throws Exception {
```

Then enable posting in the suite XML:

```xml
<parameter name="TestRailProjectId" value="223"/>
<parameter name="PostDataToTestRail" value="true"/>
```

And uncomment in:

- `TestRailUtil.java` — `startTestRailRun` / `updateTestRailResult` (uses Synergy `TestRailUpdater`)
- `SuiteListeners.onStart` — `TestRailUtil.startTestRailRun(...)`
- `TestListeners` — `TestRailUtil.updateTestRailResult(result)`

**Note:** Synergy posts results through the lab URL + your `UserKey`; confirm with your teammate that project 223 and API access are enabled for your Synergy key.

---

## Step 4 — Create a TestRail test run (manual + automation evidence)

**For Jira BSD-29441 sign-off today (email + manual link):**

1. Create a **Test Run** in TestRail: e.g. `BSD-29441 PTS Orders PROD — Aug 2026`.
2. Add the 7 Orders cases from the import.
3. After automation finishes:
   - Paste **Synergy Allure report URL** from the email into the run **Description** or each case **Comment**.
   - Mark cases **Passed** / **Failed** to match automation (or use auto-post when Step 3 is enabled).
4. Attach the email screenshot/PDF to **Jira BSD-29441** and link the TestRail run URL in a comment.

---

## Quick checklist

- [ ] CSV imported into TestRail project 223  
- [ ] Each case has TestRail ID `C#####` recorded  
- [ ] Automation ID filled in TestRail (or `@TmsLink` added in code)  
- [ ] PROD suite run completed with email report  
- [ ] TestRail run linked in Jira BSD-29441  

---

## Git repository + TestRail automation linking

Use this after the project is pushed to Git (GitHub / Bitbucket / GitLab).

### A. Push code to Git

See root `README.md`. Minimum files TestRail needs:

| Path | Purpose |
|------|---------|
| `src/test/java/.../FF_PTS_O_*.java` | Automated tests |
| `src/test/java/.../testrail/PtsTestRailCaseIds.java` | TestRail case IDs (`@TmsLink`) |
| `src/test/java/.../testrail/PtsAutomationIds.java` | Automation ID strings for TestRail |
| `docs/testrail/automation-mapping.json` | Bulk reference for QA |
| `src/test/resources/FF_PTS_BSD29441_ProdServerSuite.xml` | PROD suite |

### B. Connect Git in TestRail (Project 223)

1. **Administration** → **Integration** (or **Project Settings** → **Automation**).
2. Connect your Git provider (GitHub Enterprise / Bitbucket) and authorize Paramount org access.
3. Open **Project 223** → **Automation** / **Source** tab.
4. Set **Repository URL** to your Git repo (e.g. `https://github.com/viacomcbs/FF_Console_Automation.git`).
5. Set **Framework:** Java / TestNG.
6. Set **Source root** (if asked): `src/test/java`.

### C. Link each test case to Git source

For each manual case imported from CSV:

| TestRail field | Value |
|----------------|--------|
| **Automation Type** | Java TestNG |
| **Automation ID** | From `PtsAutomationIds.java` or CSV **Automation ID** column |
| **References** | `@TmsLink` case ID from `PtsTestRailCaseIds.java` |

Example Automation ID:

`com.paramount.test.ff.uitests.tests.tablevalidation.orders.FF_PTS_O_009_ValidateColumnSearch#validateColumnSearch`

After Git is connected, TestRail can open the **source file** in the repo from the test case Automation tab.

### D. Update case IDs after import

1. Import CSV → note each `C#####` ID.
2. Edit **one file:** `src/test/java/com/paramount/test/ff/uitests/testrail/PtsTestRailCaseIds.java`
3. Replace `C_TODO_001` … `C_TODO_017` with real IDs.
4. Commit and push — TestRail Git link stays in sync on next sync/refresh.

Also update `docs/testrail/automation-mapping.json` (optional, for documentation).

### E. Auto-post results (optional, Synergy)

When ready to push pass/fail to TestRail automatically:

1. Set `PostDataToTestRail=true` in suite XML.
2. Uncomment `TestRailUtil` + `TestListeners` hooks (see Step 3 above).
3. Re-run suite — Synergy posts results to the active TestRail run.
