# BSD-29582 — Table Header Refresh: TestRail import + automation

**Jira:** [BSD-29582](https://paramount.atlassian.net/browse/BSD-29582)  
**TestRail project ID:** `223`  
**Automation suite:** `src/test/resources/TR_TableRefresh_All_ProdServerSuite.xml`

## Step 1 — Import manual cases

1. Open TestRail project **223** → create section:  
   `Fulfillment Console > BSD-29582 > Table Header Refresh`
2. **Test Cases** → **Import** → **CSV**
3. Upload: `docs/testrail/FF_TR_TableRefresh_ManualCases_Import.csv`
4. After import, note each **TestRail case ID** (`C#####`) and update  
   `TableRefreshTestRailCaseIds.java`

| Manual ID | Scenario | Automation class |
|-----------|----------|------------------|
| FF_TR_001 | Orders — Refresh table button visible near Export | `TR_O_001_ValidateRefreshButtonVisible` |
| FF_TR_002 | Orders — filterOrder API on refresh | `TR_O_002_ValidateRefreshCallsFilterOrderApi` |
| FF_TR_003 | Orders — no duplicate rows | `TR_O_003_ValidateRefreshNoDuplicateRows` |
| FF_TR_004 | Line Items — Refresh table button visible near Export | `TR_LI_001_ValidateRefreshButtonVisible` |
| FF_TR_005 | Line Items — LineItemViewLanding API | `TR_LI_002_ValidateRefreshCallsLineItemViewLandingApi` |
| FF_TR_006 | Line Items — no duplicate rows | `TR_LI_003_ValidateRefreshNoDuplicateRows` |

## Step 2 — Run automation (PROD)

```bash
mvn test "-DsuiteXmlFile=src/test/resources/TR_TableRefresh_All_ProdServerSuite.xml"
```

## Step 3 — Email report (scenario-wise)

The suite sets `LeftFilterEmailReport=true` and each test class uses `@LeftFilterEmailScenario` so the execution email includes a table with **Manual ID**, **Scenario**, and **Status** per test.
