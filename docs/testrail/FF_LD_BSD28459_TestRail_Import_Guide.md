# BSD-28459 Feature Flag Cleanup — TestRail import + automation linking

**Jira:** [BSD-28459](https://paramount.atlassian.net/browse/BSD-28459) — [FF-Frontend] Feature flag inventory and cleanup  
**Epic:** [BSD-23415](https://paramount.atlassian.net/browse/BSD-23415) — Fulfillment Console UI - Performance Improvement  
**TestRail project ID:** `223`  
**TestRail URL:** https://paramount.testrail.io/index.php?/projects/overview/223  

**Reference format (same as PTS):** `FF_PTS_Orders_ManualCases_Import.csv` → [BSD-29441 PTS Packaging ID Orders](FF_PTS_Orders_ManualCases_Import.csv)

---

## File to import

| File | Cases | Section created in TestRail |
|------|-------|----------------------------|
| `FF_LD_BSD28459_FeatureFlagCleanup_ManualCases_Import.csv` | **32** | Fulfillment Console > BSD-28459 > Feature Flag Cleanup > … |

Location: `docs/testrail/`

Regenerate:

```powershell
python docs/tools/generate_ff_featureflag_testrail_cases.py
```

---

## Import steps in TestRail

1. Open **Test Cases** → **Import** (CSV).
2. Upload `FF_LD_BSD28459_FeatureFlagCleanup_ManualCases_Import.csv`.
3. Map columns: **Title, Section, Template, Type, Priority, References, Steps, Expected Result, Automation Type, Automation ID, Preconditions**.
4. Confirm import → verify section tree and **32** case count.

---

## Manual ID convention (mirrors FF_PTS_00x)

| Manual ID | Area | Related LD flag |
|-----------|------|-----------------|
| FF_LD_001 | Okta bootstrap | enable-okta-auth (removed) |
| FF_LD_002 | Okta token headers | add-okta-token-to-http-header |
| FF_LD_003–009 | Orders view | dynamic GraphQL, Material ID, CRID, Partner End Date, UUID, Order History v2 |
| FF_LD_010–016 | Line Items view | LI view, dynamic GraphQL, Material ID, CRID, Open Text ID, live updates |
| FF_LD_017–021 | Filter panel | refactored panel, pagination, big counts, date range |
| FF_LD_022–027 | Order history / audit | history log, audit log, revisions, revised statuses |
| FF_LD_028–029 | GLIM | sidecar files, play all |
| FF_LD_030 | Admin | test endpoint connection |
| FF_LD_031–032 | Smoke regression | end-to-end + console errors |

---

## After import (automation — same flow as PTS)

1. Note each TestRail case ID (`C#####`) → update `FeatureFlagTestRailCaseIds.java`.
2. **Automation ID** in CSV already maps to planned Java class (package `featureflag.bsd28459`).
3. Add `@TmsLink` on each `@Test` method (see PTS example `FF_PTS_O_001_ValidateColumnAvailableInTableView`).
4. Create suite XML: `src/test/resources/FF_LD_BSD28459_ProdServerSuite.xml` (when automation classes exist).
5. Run manual pass first on UAT build with BSD-28459 merged; compare behaviour to current PROD (flags were 100% on).

---

## Run automation (same pattern as PTS BSD-29441)

**Critical smoke (9 tests):**

```powershell
mvn test "-DsuiteXmlFile=src/test/resources/FF_LD_BSD28459_CriticalSmokeSuite.xml"
```

**Full regression (32 tests):**

```powershell
mvn test "-DsuiteXmlFile=src/test/resources/FF_LD_BSD28459_ProdServerSuite.xml"
```

### Code layout

| Path | Purpose |
|------|---------|
| `src/test/java/.../helpers/featureflag/` | Session, network, column, validation utils |
| `src/test/java/.../tests/featureflag/bsd28459/` | `FF_LD_001`–`FF_LD_032` test classes |
| `src/test/java/.../testrail/FeatureFlagTestRailCaseIds.java` | `@TmsLink` IDs after TestRail import |
| `src/test/resources/FF_LD_BSD28459_*Suite.xml` | TestNG suites |

Regenerate test classes after CSV changes:

```powershell
python docs/tools/generate_ff_ld_test_classes.py
```

---

| PTS (BSD-29441) | Feature Flag (BSD-28459) |
|-----------------|--------------------------|
| `FF_PTS_001` manual ID | `FF_LD_001` manual ID |
| Section `… > BSD-29441 > Orders > PTS Packaging ID` | Section `… > BSD-28459 > Feature Flag Cleanup > …` |
| `PtsTestRailCaseIds.java` | `FeatureFlagTestRailCaseIds.java` |
| `FF_PTS_BSD29441_ProdServerSuite.xml` | `FF_LD_BSD28459_ProdServerSuite.xml` (planned) |
| Column-focused validation | Regression after flag branch deletion |
