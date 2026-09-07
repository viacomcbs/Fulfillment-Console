---
name: fulfillment-console-automation
description: >-
  Builds and maintains Synergy Java TestNG automation for Fulfillment Console
  (left filters, Manage columns, table sync, DSID, table refresh, per-filter
  suites). Use when adding FC tests, XML suites, page objects, helpers, or
  automating stories like BSD-29967, BSD-29870, left-filter per-filter tests.
---

# Fulfillment Console Automation Agent

Synergy **Java + TestNG** UI automation for **Fulfillment Console** (MSC Operations Console).
Pair with manual skill: `.cursor/skills/fulfillment-console-manual-testing/SKILL.md`.
Session evidence: `docs/manual-testing/SESSION_NOTES.md`.

## Stack & layout

| Layer | Location |
|-------|----------|
| Page objects | `src/test/java/com/paramount/test/ff/pageobjects/` |
| Helpers | `src/test/java/com/paramount/test/ff/uitests/helpers/` |
| Left-filter tests | `.../uitests/tests/leftfiltersvalidation/perfilter/` |
| DSID / BSD-29967 | `.../uitests/tests/tablevalidation/dsid/`, `.../tablevalidation/bsd29967/` |
| Suite XML | `src/test/resources/regression/left-filters/orders-view/*_All_ProdServerSuite.xml`, other `*_DevServerSuite.xml` |
| Manage columns | `.../helpers/managecolumns/ManageColumnOptions.java`, `ManageColumnsUtil.java` |
| Left filters | `.../helpers/leftfilters/LeftFilterPanelUtil.java`, `LeftFilterPerFilterConfig.java` |
| Story specs | `docs/automation/BSD-29967_Automation_Spec.md` |

**Run (IntelliJ):** right-click suite XML → Run. **Maven:**  
`mvn test "-DsuiteXmlFile=src/test/resources/regression/left-filters/orders-view/LF_O_Brand_All_ProdServerSuite.xml"`

**Synergy PROD server:** `LocalExecution=false` in suite XML — browser runs on Synergy, not localhost.

## Environments (suite parameters)

| Param | Dev | UAT | PROD |
|-------|-----|-----|------|
| URL | `TargetUrlDEV` | `TargetUrlUAT` | `TargetUrlPROD` |
| Value | `dev-operationsconsole.paramountmsc.com/fulfillment/` | `uat-...` | `operationsconsole.paramountmsc.com/fulfillment/` |

Login: service account from suite `Username` / `Password`.

**Ops-console (BSD-29967):** `https://contentplatform.viacom.com/ops-console-api-dev-ui/order/{orderId}` — separate tab; may need SSO in same browser session.

---

## Calendar date rules (critical)

| Suite / story | Date preset | Helper |
|---------------|-------------|--------|
| Left-filter per-filter (Brand, Submitted By, Assigned To, …) | **Yesterday** | `CalendarSetupUtil.setYesterdayDefaultBookmarkOnce()` + `LeftFilterSessionHelper.markCalendarSetToYesterday()` |
| BSD-29967 multi-submission DSID | **Yesterday** | Same as left-filter (via `Bsd29967SessionHelper` / base test) |
| BSD-29870 DSID | **Today** | `DsidCalendarSetup.setTodayAsDefaultOnce()` + `DsidSessionHelper` |
| Manual QA (any story) | Often **Today** | N/A — automation deliberately differs for stable data |

**Why Yesterday for filters / BSD-29967:** Dev **Today** has live ingest — row counts and status chips shift during a run. Yesterday gives a stable snapshot.

---

## Shared utilities — use these, do not duplicate

### Manage columns

- **`ManageColumnOptions`** — canonical column labels and section arrays.
- **`ManageColumnsUtil`** — open/close panel, enable column, scroll panel, save, confirm Added Columns modal.
- **`AutomationTableViewSetupUtil`** — once-per-session column setup via `LeftFilterSessionHelper`.
- **`TableView.getTableViewButton()`** — `#tableViewButton` gear XPath.

**Frequently used constants:**

| Constant | Label | Manage columns section |
|----------|-------|------------------------|
| `SUBMITTED_BY` | Submitted By | **ORDER** (Order columns accordion) |
| `ASSIGNED_TO` | Assigned to | **ORDER_LINE_ITEM** (flat list above LINE ITEM COLUMNS) |
| `ACTIVITY_TYPE` | Activity Type | **ORDER_LINE_ITEM** |
| `ORDER_START_DATE` | Order start date | **ORDER** |
| `DSID` | DSID | **ORDER** |
| `AUTOMATION_VIEW_NAME` | AAAAA | Saved view (UI may show `Aautomation`) |

**`defaultSectionForColumn(label)`** — never mix ORDER vs ORDER_LINE_ITEM when enabling columns.

**Patterns learned:**

- Call `ensureColumnEnabledForView()` — skips Manage columns if column already on grid (`isOrderColumnVisibleOnGrid`).
- Horizontal scroll grid before assuming column missing (`scrollOrdersGridForColumn` / JS scroll in util).
- **Save changes** disabled = no pending edits; do not fail if nothing to save.
- Scroll Manage columns panel to reach checkboxes below fold (e.g. DSID, Order start date, Assigned to).
- Saved view: enable column on **current** view — do not assume a specific view name unless story requires it.

### Left filters

- **`LeftFilterPanelUtil`** — expand/select/search/scroll/active-filters/table-sync for all filters.
- **`LeftFilterPerFilterConfig`** — per-filter `FilterSpec` (display name, table column, skipped categories).
- **`LeftFilterPerFilterTestRunner`** — wires TC categories to test methods (generic filters).
- **`LeftFilterSessionHelper`** — session flags (manage column ready, bookmark date, etc.).
- **`CalendarSetupUtil`** — date bookmark setup (**Yesterday** for left-filter suites).

**Dynamic options:**

- `resolveFirstNonZeroFilterOption(filterName)` — first checkbox with count &gt; 0 (skips literal `All`).
- `resolveFirstSelectableFilterOption(filterName)` — non-zero, or first non-`All` label.
- **Assigned To only:** `resolveFirstNonZeroPersonFilterOption` (internal) — skips `All` **and** `Unassigned`.
- **Assigned To only:** `resolveUnassignedFilterOption` (internal) — `Unassigned` with count &gt; 0.

**Active filters (TC10xx):** expand **Active filters** button so `#collapsePanel` chips are visible before asserting chips.

**Environment TC603 / TC609:** count-only or custom smoke — Environment is **not** on Orders grid; no column value checks.

**Clear filters (TC11xx):** use `WaitUtil.isDisplayFast` for post-clear UI settle (Environment suite had 2‑min wait bug — fixed).

**Initials helper (shared):** `toSubmittedByTableInitials(fullName)` — e.g. `Akilandeswari Sundararajan` → `AS`. Reused for **Submitted By** and **Assigned To** (person path).

---

## Per-filter test categories (TC numbering)

From `LeftFilterTestCategory`:

| Offset | Category | Submitted By (idx 5) | Assigned To (idx 9) |
|--------|----------|----------------------|---------------------|
| 1xx | BASIC | TC105 | TC109 |
| 1x+1 | OPTION_ORDER | TC106 | TC110 |
| 2xx | SEARCH | TC205 | TC209 |
| 4xx | SELECT_ALL | TC405 | TC409 |
| 6xx | TABLE_SYNC | TC605 | TC609 |
| 8xx | SCROLL | TC805 | TC809 |
| 10xx | ACTIVE_FILTERS | TC1005 | TC1009 |
| 11xx | CLEAR_FILTERS | TC1105 | TC1109 |

Filter index = position in `OrdersLeftFilter` enum (1-based for TC id middle digit).

**New filter checklist** (copy Submitted By or Assigned To):

1. Add `FilterSpec` in `LeftFilterPerFilterConfig` — set `tableColumn` to `ManageColumnOptions.*` constant (not a search string).
2. Create `LeftFilter{Filter}OrdersTabBaseTest` — override `manageColumnsColumnToEnable()` when grid column is required.
3. Add 8 test classes under `perfilter/orders/{filter}/`.
4. For **TABLE_SYNC**: if filter label ≠ grid cell text (initials, Unassigned, etc.), add dedicated `validate{X}TableSyncSmoke()` in `LeftFilterPanelUtil` — do **not** use generic `validateTableRecordsMatchFilter`.
5. Create `regression/left-filters/orders-view/LF_O_{Filter}_All_ProdServerSuite.xml` (mirror `LF_O_SubmittedBy_All_ProdServerSuite.xml`).
6. Register TestRail IDs if applicable.

**Suite XML policy:** one **per-filter PROD** XML per filter under `regression/left-filters/orders-view/` (`LF_O_SubmittedBy_All_ProdServerSuite.xml`, `LF_O_AssignedTo_All_ProdServerSuite.xml`). Do **not** rely on mega generated `LeftFilterPerFilter_Dedicated_DevServerSuite.xml` — prefer dedicated PROD suites for CI/Synergy runs.

---

## Orders grid — automation learnings

| UI behavior | Automation approach |
|-------------|---------------------|
| Click **non-Status cell** | Opens Details panel; URL → `/fulfillment/focus?tableView=orders&order={orderId}__{env}` — parse Order ID + environment from URL |
| Click **row expand chevron** | Inline Package ID / line-items — use for Activity Type TC620 expanded-row reads, **not** Details panel |
| Click **Status** column / status chips | Status breakdown — avoid for details / order selection |
| Status header gear | Often opens Manage columns — use `#tableViewButton` instead when possible |
| **(i)** next to Order start date | Multi-submission order (BSD-29967) — scan via JS in `Bsd29967ColumnUtil` |

**Locators:** prefer XPath in page objects; `LeftFilterPanel.tableColumnHeader(label)` / `tableColumnCells(label)`.

**Details panel DSIDs:** `DsidColumnUtil.readAllDetailsPanelDsids()` uses DOM + `readDsidValuesViaJs()` fallback — **no scroll required** in automation (manual QA scrolls inner Order details panel).

---

## Filter-specific table sync rules

### Submitted By (implemented)

- Filter shows **full name** (e.g. `Akilandeswari Sundararajan`).
- Grid **Submitted By** column shows **initials only** (e.g. `AS`).
- `validateSubmittedByTableSyncSmoke()` — first non-zero option → count sync → initials on every visible cell.
- Base: `LeftFilterSubmittedByOrdersTabBaseTest` → `ManageColumnOptions.SUBMITTED_BY`.
- Suite: `LF_O_SubmittedBy_All_ProdServerSuite.xml`.

### Assigned To (implemented — 2026-09-01)

**Two dynamic paths** (PROD data varies):

| PROD state | Filter selection | Table assertion |
|------------|------------------|-----------------|
| Named person with count &gt; 0 | First non-zero **person** (skips `Unassigned`) | **Assigned to** cells = **initials** (`toSubmittedByTableInitials`) |
| No person with count &gt; 0 | **Unassigned** (below Select all, count &gt; 0) | All visible **Assigned to** cells **blank** (`""`, `-`, `—`) |

- `validateAssignedToTableSyncSmoke()` in `LeftFilterPanelUtil`.
- Base: `LeftFilterAssignedToOrdersTabBaseTest` → `ManageColumnOptions.ASSIGNED_TO` (ORDER_LINE_ITEM section).
- TC609 calls smoke directly (not `LeftFilterPerFilterTestRunner` TABLE_SYNC).
- Suite: `LF_O_AssignedTo_All_ProdServerSuite.xml`.
- `FilterSpec` table column = `ManageColumnOptions.ASSIGNED_TO` (fixed from old `"unassign"` mistake).

### Brand / most order-level filters

- Filter label matches table column text 1:1.
- Enable column via base test + `ManageColumnsUtil`.
- TABLE_SYNC via `LeftFilterPerFilterTestRunner` + `validateTableRecordsMatchFilter`.

### Environment

- Count-only TABLE_SYNC — no Manage columns; `validateEnvironmentTableSyncSmoke()`.

### Activity Type (TC620)

- Line-item column on Orders tab (ORDER_LINE_ITEM flat list).
- Table sync reads **expanded row** line-item values; may use Details panel Line Item ID for cross-check.

---

## DSID — BSD-29870 (existing)

- **`DsidPage`** — grid + details panel DSID locators (`demandSystemId-value` spans).
- **`DsidColumnUtil`** — enable DSID column, open details, compare **grid vs last** Details DSID.
- **`readAllDetailsPanelDsids(softAssert)`** — all Details values (also used by BSD-29967).
- **`DsidSessionHelper`** + **`DsidSuiteListener`** — shared session + email report.
- Suite: `FF_DSID_BSD29870_ProdServerSuite.xml`.
- Filters: Job type = MetadataOnlyDelivery, Partner = PP YT FSP UK; date = **Today**.

---

## BSD-29967 — multi-submission DSID vs ops-console (implemented)

**Full spec:** `docs/automation/BSD-29967_Automation_Spec.md`  
**Jira:** BSD-29967 | **Suite:** `FF_BSD29967_DevServerSuite.xml` (Dev first; clone for PROD after sign-off)

### Code map

| Artifact | Purpose |
|----------|---------|
| `Bsd29967OrdersBaseTest` | Yesterday + first non-zero Environment + shared session |
| `FF_BSD29967_O_001` | (i) row → Details → DSIDs non-empty |
| `FF_BSD29967_O_002` | ops-console latest job `dsIdList` vs FC Details (set compare) |
| `Bsd29967ColumnUtil` | Environment filter, Order start date column, (i) row scan, open Details |
| `Bsd29967DetailsUtil` | Parse `order={id}__{env}` from URL; normalize/compare DSID lists |
| `Bsd29967OpsConsoleUtil` | New tab → Workflow = Environment → Dynamo → parse `jobs[]` JSON from DOM |
| `Bsd29967SessionHelper` | Order id, env, DSID lists, omfOrderId, window handles |
| `Bsd29967SuiteListener` + `Bsd29967EmailReport` | Email report with Environment / both DSID lists / Match Y/N |
| `Bsd29967Page`, `OpsConsolePage` | Grid (i) icon, ops-console Workflow / Dynamo locators |

### Flow summary

| Step | Implementation |
|------|----------------|
| Date | **Yesterday** — `CalendarSetupUtil` + `Bsd29967SessionHelper` (not `DsidCalendarSetup` Today) |
| Environment | `applyFirstNonZeroEnvironmentFilter()` — dynamic (e.g. FSPGLOBAL) |
| Manage columns | **Order start date** required for (i) scan (`ManageColumnOptions.ORDER_START_DATE`) |
| Find row | JS scan Order start date column for info icon (`bi-info`, `msc-info`, etc.) |
| Open Details | JS click first non-Status `td` on row — verify `/focus?order=` URL |
| Read FC DSIDs | `DsidColumnUtil.readAllDetailsPanelDsids()` — all values, not last only |
| Ops-console | `window.open` → select Workflow → wait 30–60s → Dynamo tab → extract `jobs[]` from page JSON |
| Latest job | Highest `lastUpdatedTime` in `jobs[]` (fallback `startTime`) |
| Compare | Full `dsIdList` `dsId` values vs FC Details — sorted set equality after normalize |
| Phase 2 | Loop all non-zero Environments; post results table to Jira |

**Multi-submission visual:** `9/1/2026, 4:18:19 PM  (i)` immediately right of Order start date.

**Manual reference (Match Y):** Order **4684082**, FSPGLOBAL, 9 DSIDs — `HD_GENERIC_PPLUS20_23_MULTI`, `UHDCBSFRC117A1`…`A6`, `UHDCBSFRC117DUBSES419`, `UHDCBSFRC117A`.

**BSD-29870 vs BSD-29967:**

| | BSD-29870 | BSD-29967 |
|---|-----------|-----------|
| Compare | Grid DSID vs **last** Details DSID | **All** Details DSIDs vs ops-console `dsIdList` |
| Date | Today | Yesterday |
| Filters | Job type + Partner | Environment (first non-zero) |
| Second app | None | ops-console Dev UI |

---

## Suite XML templates

### Left-filter PROD (shared session)

```xml
<parameter name="TestEnvironment" value="PROD"/>
<parameter name="LocalExecution" value="false"/>
<parameter name="LeftFilterEmailReport" value="true"/>
<parameter name="LeftFilterEmailSuiteTitle" value="Orders Tab — {Filter} all per-filter tests (PROD)"/>
<!-- TargetUrlPROD, Username, Password, Synergy UserKey — copy from LF_O_SubmittedBy_All_ProdServerSuite.xml -->
<listeners>
  <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>
</listeners>
```

Use `preserve-order="true"`, `parallel="false"`. Include explicit `<methods><include name="..."/></methods>` per class.

### BSD-29967 Dev

```xml
<parameter name="TestEnvironment" value="DEV"/>
<parameter name="JiraTicketID" value="BSD-29967"/>
<listener class-name="com.paramount.test.ff.common.listeners.Bsd29967SuiteListener"/>
```

Maven: `mvn test "-DsuiteXmlFile=src/test/resources/FF_BSD29967_DevServerSuite.xml"`

### Implemented PROD left-filter suites

| Filter | Suite XML (under `regression/left-filters/orders-view/`) |
|--------|-----------|
| Submitted By | `LF_O_SubmittedBy_All_ProdServerSuite.xml` |
| Assigned To | `LF_O_AssignedTo_All_ProdServerSuite.xml` |
| Brand | `LF_O_Brand_All_ProdServerSuite.xml` |
| Environment | `LF_O_Environment_All_ProdServerSuite.xml` |
| Activity Type | `LF_O_ActivityType_All_ProdServerSuite.xml` |
| Line Item Status | `LF_O_LineItemStatus_All_ProdServerSuite.xml` |
| Order Status | `LF_O_OrderStatus_All_ProdServerSuite.xml` |

---

## Common pitfalls

| Pitfall | Fix |
|---------|-----|
| Stale classes after helper change | `mvn test-compile` before Synergy run |
| Manage columns opens from wrong click | Target `#tableViewButton`, not Status gear |
| Column off-screen | Horizontal scroll grid / `scrollOrdersGridUntilColumnVisible` |
| Wrong Manage columns section | Use `ManageColumnOptions.defaultSectionForColumn` — Assigned to is ORDER_LINE_ITEM |
| Active filter chip not found | Expand **Active filters** panel first |
| Environment / clear filters hang | `WaitUtil.isDisplayFast`, not long fixed sleep |
| Submitted By / Assigned To sync fails | Compare **initials**, not full name |
| Assigned To sync fails on Unassigned | Use person path only when non-zero person exists; else Unassigned + blank cells |
| Row expand confused with Details | Different locators; verify `/focus` URL for Details |
| `pointer-events: none` on gear | Wait for overlay close; scroll table header into view |
| BSD-29967 uses Today calendar | Use Yesterday — live Today ingest shifts rows on Dev |
| BSD-29967 compares top-level `dsId` | Use full **`dsIdList`** from latest job only |
| Ops-console slow / empty JSON | 30–60s after Workflow change; JS scans `pre`/body for `jobs` array |
| Generic TABLE_SYNC for initials filters | Dedicated `validate*TableSyncSmoke()` — runner's `validateTableRecordsMatchFilter` expects 1:1 label match |
| Mega DEV suite | Prefer `regression/left-filters/orders-view/LF_O_{Filter}_All_ProdServerSuite.xml` per filter |

---

## New code conventions

- **Minimize diff** — extend existing helpers before new abstractions.
- **Column names** — always `ManageColumnOptions` constants.
- **SoftAssert** — match existing `LeftFilterPanelUtil` / suite listener patterns.
- **Logging** — `Logger.logReportMessage` for option labels, counts, column values, DSID lists (aids Synergy report debugging).
- **Test class names** — `LF_{O|LI}_TC{id}_{Filter}_{Category}Test.java` or `FF_{STORY}_{O|LI}_{nnn}_*.java`.
- **Email reports** — `LeftFilterEmailReport`, `DsidEmailReport`, `Bsd29967EmailReport`; wire in `SuiteListeners` / dedicated suite listener.
- **Do not commit** credentials; suite XML holds Synergy params (repo convention).

---

## When to update docs

After automation work:

1. Append UI quirks to `docs/manual-testing/SESSION_NOTES.md`.
2. Update story specs under `docs/automation/` when flow changes.
3. Update **this skill** when new filters, suites, or cross-app patterns (ops-console, initials, Unassigned) are added.
