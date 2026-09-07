# BSD-29967 — Automation Specification

**Story:** [BSD-29967](https://paramount.atlassian.net/browse/BSD-29967)  
**Goal:** Validate FC Details panel **DSID** values match ops-console **Jobs → dsIDlist** for orders submitted **multiple times**, per **Environment** workflow.  
**Status:** Ready to implement (manual flow explored 2026-09-01)  
**Agent skills:** `.cursor/skills/fulfillment-console-automation/SKILL.md`, `.cursor/skills/fulfillment-console-manual-testing/SKILL.md`

---

## 1. Test scope

| In scope | Out of scope (unless story says otherwise) |
|----------|---------------------------------------------|
| Orders tab | Line items tab |
| Dev FC + ops-console Dev UI (initial) | PROD until Dev sign-off |
| First **non-zero** Environment option per iteration | All 14 environments in one run (optional loop) |
| Orders with **(i)** icon on Order start date | Single-submission orders |
| DSID list compare FC Details vs ops-console | Other Details fields |

---

## 2. Environments & URLs

| System | URL |
|--------|-----|
| FC Dev | `https://dev-operationsconsole.paramountmsc.com/fulfillment/` |
| Ops Console API Dev UI | `https://contentplatform.viacom.com/ops-console-api-dev-ui/order/{orderId}` |
| Example order | `.../order/4590424` (user reference) |

**Suite parameter:** start with `TestEnvironment=DEV` and `TargetUrlDEV`; mirror `FF_DSID_BSD29870_ProdServerSuite.xml` structure.

---

## 3. Preconditions (session setup)

1. **Login** — service account (same pattern as DSID / left-filter suites).
2. **Orders tab** — `OrdersTabTestSetupHelper` / `ConsoleTab.ORDERS`.
3. **Date = Yesterday only** — **not** Today (manual QA uses Today; automation uses Yesterday for stable data).
   - Reuse `CalendarSetupUtil.setYesterdayDefaultBookmarkOnce()` + `LeftFilterSessionHelper.markCalendarSetToYesterday()` (same as left-filter / PTS suites).
   - Do **not** call `DsidCalendarSetup.setTodayAsDefaultOnce()` for BSD-29967.
4. **Left filter: Environment** — select **first non-zero count** option dynamically.
   - Reuse `LeftFilterPanelUtil.resolveFirstNonZeroFilterOption("Environment")`.
   - Option names vary (e.g. `FSPGLOBAL`); never hardcode unless smoke-only.
5. **Manage columns** — ensure visible on Orders grid:
   - **Order start date** (required — exposes **(i)** icon).
   - **Order ID** (helpful for logging / URL cross-check).
   - **DSID** (optional on grid; Details panel is source of truth for compare).
   - Use `ManageColumnsUtil.ensureColumnEnabledForView()`; skip if already on grid.
6. **Table view** — current saved view (e.g. AAAAA / Aautomation); no need to select-all columns.

---

## 4. Manual flow → automation mapping

| # | Manual step | Automation implementation |
|---|-------------|---------------------------|
| 1 | Open FC Dev, authenticate | `DriverUtil.launchApplicationOnBrowser()` + `Login.loginToFF()` |
| 2 | Set date **Yesterday** (automation) | `CalendarSetupUtil.setYesterdayDefaultBookmarkOnce(softAssert)` + `LeftFilterSessionHelper.markCalendarSetToYesterday()` |
| 3 | Environment filter → first non-zero option | `expandFilter("Environment")` → `resolveFirstNonZeroFilterOption` → `selectFilterOption` → store label in session |
| 4 | Verify active chip `Environment: {name}` | `LeftFilterPanelUtil` active-filters helpers (expand `#collapsePanel` first) |
| 5 | Ensure **Order start date** column on grid | `ManageColumnsUtil.ensureColumnEnabledForView(softAssert, "Order start date")` |
| 6 | Find row with **(i)** next to Order start date | **New:** scan Order start date cells for info icon (see §6) |
| 7 | Click **non-Status** cell on that row | Click Season / Episode / Order start date cell — **not** Status, **not** row expand chevron |
| 8 | Details panel opens | Assert URL contains `/fulfillment/focus?tableView=orders&order={id}__{env}` |
| 9 | Parse Order ID + environment from URL | **New:** `order=4624680__fspglobal` → id `4624680`, env `fspglobal` |
| 10 | Scroll Details panel; read all **DSID** values | Extend `DsidColumnUtil.readAllDsidValuesFromDetailsPanel()` / `DsidPage` |
| 11 | New tab: ops-console order URL | `driver.get().browser().openNewTab()` or Synergy multi-window |
| 12 | Workflow dropdown = same Environment | **New:** `OpsConsolePage` locator + select stored env label |
| 13 | Wait for slow page load | Generous wait (30–60s) after workflow change |
| 14 | **Dynamo** tab → **Jobs** array | User correction: **Jobs**, not "Gobs" |
| 15 | Open **latest** job → **dsIdList** | **Latest job** = highest `lastUpdatedTime` in `jobs[]`. Expand **`dsIdList`** on that job only. |
| 16 | **Capture all `dsId` values** | Every object in `dsIdList`: `{ assetType, dsId }`. Example (order 4590424): `UHDLION302X1`…`UHDLION302X5`, `UHDLION302DUBSFRFR`, `UHDLION302X`, `HD_ORIGINAL_PPLUS28_23_MULTI`. |
| 17 | Compare FC DSIDs vs dsIdList | Set compare after normalization; soft-assert per environment |
| 17 | (Optional) Repeat for next non-zero Environment | Loop or parameterized test — names dynamic |

---

## 5. Multi-submission indicator — (i) icon

**Visual pattern (user reference + Dev confirmation):**

```
Order start date column:
  9/1/2026, 4:18:19 PM  (i)
```

| Property | Detail |
|----------|--------|
| Location | Immediately **right** of Order start date timestamp in Orders grid |
| Meaning | Order submitted **multiple times** |
| Frequency | Not every row — must scan after Environment + **Yesterday** filter (automation) |
| Correlation | Often high in-progress counts in Status (e.g. 3↻, 5↻, 8↻) — use Status only as hint, not selector |

**Automation locator ideas (to validate in DevTools):**

- XPath near Order start date cell: `//td[contains(@class,'order-start') or .//span[contains(@class,'order-start')]]//i[contains(@class,'bi-info') or contains(@class,'info')]`
- Or: cell containing timestamp + sibling/parent with `bi-info-circle` / `msc-info` (confirm in DOM)
- Fallback: JS scan visible rows — cell text matches date pattern + has child with info icon class

**Add constant:** `ManageColumnOptions.ORDER_START_DATE = "Order start date"` (string exists in `ORDER_COLUMNS[]` only today).

---

## 6. Orders grid — click rules (critical)

| Click target | Result | Use in BSD-29967? |
|--------------|--------|-------------------|
| Season, Episode, Order start date, Order ID, etc. | **Details panel** (right drawer) | **Yes** |
| Row expand chevron (caret) | Inline Package ID / line-items table | **No** |
| Status column / status chips | Status breakdown overlay | **No** |
| Status header gear | Manage columns (mis-click risk) | **No** — use `#tableViewButton` |

**Details panel URL pattern:**

```
/fulfillment/focus?tableView=orders&order={orderId}__{environmentLowercase}
```

**Verified examples (Dev / FSPGLOBAL / 2026-09-01):**

| Order ID | Environment | Title | Notes |
|----------|-------------|-------|-------|
| 3780056 | fspglobal | FBI S1 E120 | Details opened; may be single submission |
| 4624680 | fspglobal | The Challenge S42 E4206 | Details opened; near (i) rows |

---

## 7. Details panel — DSID extraction

**Reuse from BSD-29870 (`DsidColumnUtil` / `DsidPage`):**

- `detailsPanelDsidSection()` — label `DSID`
- `detailsPanelDsidValues()` — `demandSystemId-value` spans
- `readAllDsidValuesFromDetailsPanel()` + `readDsidValuesViaJs()` fallback
- Scroll panel before read (Page Down / JS `scrollTop` on details container)

**BSD-29967 difference from BSD-29870:**

- BSD-29870: compare **grid DSID** vs **last** Details DSID.
- BSD-29967: collect **all** Details DSIDs vs ops-console **dsIDlist** (full list compare, order may matter — confirm with story).

**Normalization:** trim, collapse whitespace, ignore `-` empty placeholders (existing `isPopulatedDsid()` pattern).

---

## 8. Ops-console flow (new automation)

| Step | UI | Notes |
|------|-----|-------|
| Navigate | `/ops-console-api-dev-ui/order/{orderId}` | Same order ID from FC URL |
| Workflow | Dropdown at top | Select **exact** Environment label from FC filter (e.g. `FSPGLOBAL`) |
| Load wait | Full page | Slow — allow 30–60s after workflow select |
| Tab | **Dynamo** | |
| Section | **Jobs** array | Not "Gobs" |
| Job | **Latest** (first / most recent — confirm sort) | |
| Field | **dsIDlist** | Array of DSID strings |
| Compare | vs FC Details DSIDs | Match Y/N; log both lists in report |

**New artifacts:**

- `OpsConsolePage.java` — workflow dropdown, Dynamo tab, Jobs expand, dsIDlist read
- `Bsd29967OpsConsoleUtil.java` — navigate, select workflow, extract dsIDlist
- May need **second browser tab/window** — confirm Synergy API for tab switch

---

## 9. Reuse existing code

| Existing | Reuse for BSD-29967 |
|----------|---------------------|
| `CalendarSetupUtil` + `LeftFilterSessionHelper` | Yesterday date setup (automation) |
| `DsidPage` / `DsidColumnUtil` | Details panel DSID read (extend for all values) |
| `ManageColumnsUtil` | Order start date column enable |
| `LeftFilterPanelUtil` | Environment filter, first non-zero option |
| `LeftFilterOrdersTabBaseTest` | Yesterday calendar + session pattern (prefer over `DsidOrdersBaseTest`, which sets Today) |
| `DsidSuiteListener` | Email report pattern — create `Bsd29967SuiteListener` if needed |
| `FF_DSID_BSD29870_ProdServerSuite.xml` | Suite XML template (change Jira id, Dev URL, test classes) |

---

## 10. Proposed new code structure

```
src/test/java/com/paramount/test/ff/
├── pageobjects/
│   ├── Bsd29967Page.java          # (i) icon, order-start-date cell, focus URL helpers
│   └── OpsConsolePage.java        # ops-console Dynamo/Jobs/dsIDlist
├── uitests/helpers/bsd29967/
│   ├── Bsd29967SessionHelper.java
│   ├── Bsd29967ColumnUtil.java    # manage columns + find multi-submission row
│   ├── Bsd29967DetailsUtil.java   # open details, parse URL, read DSIDs
│   ├── Bsd29967OpsConsoleUtil.java
│   └── Bsd29967EmailReport.java   # optional — results table in email
└── uitests/tests/tablevalidation/bsd29967/
    ├── Bsd29967OrdersBaseTest.java
    └── FF_BSD29967_O_001_ValidateDsidMatchesOpsConsoleJobs.java

src/test/resources/
└── FF_BSD29967_DevServerSuite.xml   # or Prod after sign-off
```

**Suggested test methods (single suite, shared session):**

1. `setupYesterdayAndEnvironment` — calendar + first non-zero Environment
2. `enableOrderStartDateColumn` — Manage columns once per session
3. `findAndOpenMultiSubmissionOrder` — (i) row → Details panel
4. `validateDsidMatchesOpsConsoleDsIdList` — FC vs ops-console compare

Or one test class with ordered steps (like DSID O_001 → O_002 chain).

---

## 11. Assertions & reporting

**Per Environment iteration, log and soft-assert:**

```markdown
| Environment | Order ID | Order start date | DSIDs (FC Details) | dsIDlist (Jobs) | Match |
|-------------|----------|------------------|--------------------|-----------------|-------|
| FSPGLOBAL   | 4624680  | 9/1/2026, 6:… PM | [dsid1, dsid2]     | [dsid1, dsid2]  | Y     |
```

**Assertions:**

- At least one row with **(i)** found (or skip env with clear message).
- Details panel opens (`focus` URL).
- FC Details DSID list **not empty**.
- ops-console dsIDlist **not empty**.
- Sets equal after normalization (or ordered list equal — confirm with BA).

**Email report:** follow `DsidEmailReport` / `LeftFilterEmailReport` pattern; include Environment, Order ID, both DSID lists, Match Y/N.

---

## 12. Pitfalls (from manual sessions)

| Issue | Mitigation |
|-------|------------|
| Narrow viewport / mis-clicks | Synergy desktop — ensure adequate resolution; avoid Status column clicks |
| `#tableViewButton` `pointer-events: none` | Wait for overlays to close; scroll header into view |
| Filter sidebar gear ≠ Manage columns | Use `#tableViewButton` only |
| Row expand vs Details | Target non-Status cell XPath; verify URL changes to `/focus` |
| Order start date off-screen | `scrollOrdersGridForColumn("Order start date")` before (i) scan |
| Manage columns checkbox below fold | Scroll panel before click (DSID, Order start date) |
| Save changes disabled | Expected when no edits — do not fail |
| Environment clear long wait | Use `WaitUtil.isDisplayFast`, not 2-min sleep |
| ops-console slow load | Explicit wait after Workflow change |
| Dynamic Environment names | Never hardcode; store from filter option label |
| Stale compiled classes | Rebuild before Synergy run |

---

## 13. Test data notes (Dev)

| Filter | Manual | Automation |
|--------|--------|------------|
| Date | **Today** (e.g. `2026-09-01`) | **Yesterday** only |
| Environment | FSPGLOBAL (first non-zero; ~726–728 results on Today manual run) | First non-zero (dynamic) |
| (i) rows | Multiple rows with `(i)` next to Order start date when column visible | Date-dependent — tolerate “no (i) row” with clear skip/fail |

Data is **date- and env-dependent** — tests must tolerate “no (i) row” with clear skip/fail message.

---

## 14. Open questions (confirm before coding)

- [ ] Compare **all** DSIDs or **last** only? → **All** values from latest job `dsIdList` vs FC Details panel.
- [ ] **Latest job** definition → **Highest `lastUpdatedTime`** in `jobs[]` (confirmed user ref 4590424).
- [ ] Run **one** Environment per suite or **loop all** non-zero options?
- [ ] **Dev only** first, then UAT/PROD suite clone?
- [ ] TestRail case IDs — create new or link to BSD-29967 story?
- [ ] ops-console auth — same service account or separate login step?

---

## 15. Implementation checklist

```
Phase 1 — FC only
- [ ] Add ManageColumnOptions.ORDER_START_DATE constant
- [ ] Bsd29967Page — (i) icon locator + multi-submission row finder
- [ ] Bsd29967ColumnUtil — enable Order start date; scroll grid; find (i) row
- [ ] Bsd29967DetailsUtil — click non-Status cell; parse focus URL; read all DSIDs
- [ ] Bsd29967OrdersBaseTest — Yesterday + Environment setup
- [ ] FF_BSD29967_O_001 — open Details for (i) row; assert DSIDs non-empty

Phase 2 — ops-console
- [ ] OpsConsolePage + Bsd29967OpsConsoleUtil
- [ ] Tab/window switch to ops-console URL
- [ ] Workflow select + Dynamo → Jobs → latest → dsIDlist
- [ ] FF_BSD29967_O_002 — full compare + results table

Phase 3 — suite & reporting
- [ ] FF_BSD29967_DevServerSuite.xml
- [ ] Bsd29967SuiteListener + email report
- [ ] TestRail IDs (if required)
- [ ] Update SESSION_NOTES + automation skill after first green run
```

---

## 16. Related docs

- Manual session evidence: `docs/manual-testing/SESSION_NOTES.md`
- DSID prior story automation: `FF_DSID_BSD29870_ProdServerSuite.xml`
- Manage columns frames: `docs/manage_column_video_frames/`
