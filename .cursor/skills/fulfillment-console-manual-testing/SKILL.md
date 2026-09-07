---
name: fulfillment-console-manual-testing
description: >-
  Guides manual QA on Fulfillment Console (Orders/Line Items, filters, Manage
  columns, details panel, multi-submission orders, ops-console DSID checks).
  Use when the user asks for FC manual testing, BSD story sign-off, browser
  walkthrough, or documenting manual test learnings.
---

# Fulfillment Console Manual Testing Agent

Manual QA playbook for **Fulfillment Console** (MSC Operations Console → Fulfillment).
Use browser MCP or drive the user through steps. Record new findings in
[SESSION_NOTES.md](../../../docs/manual-testing/SESSION_NOTES.md).

## Environments

| Env | URL |
|-----|-----|
| Dev | `https://dev-operationsconsole.paramountmsc.com/fulfillment/` |
| Prod | `https://operationsconsole.paramountmsc.com/fulfillment/` |
| Ops Console API Dev UI | `https://contentplatform.viacom.com/ops-console-api-dev-ui/order/{orderId}` |

## Browser setup

1. Resize viewport to **1920×1080** before interacting.
2. Widen the Cursor browser panel if the table looks cramped.
3. **Do not** click the Status column or Status-header gear when opening order details.

## Standard session checklist

```
- [ ] User authenticated (service account or SSO)
- [ ] Correct tab: Orders vs Line items
- [ ] Date range set (story-specific — **BSD-29967 manual = Today only**; automation uses Yesterday)
- [ ] Left filter applied (if required)
- [ ] Manage columns view: Aautomation (or story-specific view)
- [ ] Required columns visible in grid
- [ ] Action performed + evidence captured (screenshot / table)
```

## Orders grid — critical interactions

| User action | Result |
|-------------|--------|
| Click **non-Status cell** (Season, Episode, Order start date, Order ID, etc.) | **Right-side Details panel** opens; URL → `/fulfillment/focus?tableView=orders&order={orderId}__{env}` |
| Click **row expand chevron** (caret) | **Inline** line-items expansion (Package ID sub-table) — not the Details panel |
| Click **Status column** or status chips | Status breakdown popup — not the Details panel |
| Click **gear in Status header** | Manage columns or status controls — easy to mis-click |

**Order ID from URL:** `order=4624680__fspglobal` → Order ID **4624680**, environment **fspglobal**.

## Multi-submission order — (i) icon

**Story:** BSD-29967 (DSID sync with ops-console Jobs → dsIDlist).

A **multi-submission** order shows a small **(i)** info icon **immediately to the right** of the **Order start date** timestamp:

```
9/1/2026, 4:18:19 PM  (i)
```

- The icon is **only** on some rows — not every order.
- Requires **Order start date** column enabled in Manage columns.
- Scroll the grid **horizontally** if the column is off-screen.
- Rows with (i) often also show multiple in-progress counts in Status (e.g. 3↻, 5↻).

## BSD-29967 — calendar date rule

| Mode | Date preset | Notes |
|------|-------------|-------|
| **Manual testing** | **Today** (single day) | Scan Order start date for **(i)** on current-day data |
| **Automation** | **Yesterday** only | Avoids live-ingest churn (counts/rows shifting during run) |

**Live records on Today (manual):** result total and status chips (e.g. In progress) **increase while you test** — normal. Mitigations:

1. **Search by Order ID** once you spot a row with **(i)** — grid stops shifting under you.
2. Work **quickly** after opening Details (DSID + ops-console compare before more jobs land).
3. **Do not** assert exact result count — only that Environment filter + row-level data are correct.
4. **Refresh table** only if the grid looks stale; each refresh can reorder rows.
5. If testing is blocked by churn, note it in evidence — automation will use **Yesterday** for stability.

**Find workflow (Environment filter + Today — manual only):**

1. Set date = **Today** (single day).
2. Environment filter → pick **first non-zero** option (e.g. **FSPGLOBAL**).
3. Ensure **Order start date** column is checked (Aautomation view).
4. Scan Order start date column for **(i)**.
5. Click a **non-Status** cell on that row → Details panel.
6. Scroll Details panel for **DSID** values.
7. New tab: ops-console URL with same Order ID → Workflow = same Environment → **Dynamo** → **Jobs** → **latest job** (max `lastUpdatedTime`) → **dsIdList** → compare.

**Latest job rule (ops-console):** When `jobs` has multiple entries, select the one with the **highest `lastUpdatedTime`**. Read **`dsIdList`** from that job only.

**dsIdList capture (required evidence):** Expand `dsIdList` on the latest job and record **every `dsId`** (+ `assetType`). Example reference order 4590424:

```
UHDLION302X1, UHDLION302X2, UHDLION302X3, UHDLION302X4, UHDLION302X5,
UHDLION302DUBSFRFR, UHDLION302X, HD_ORIGINAL_PPLUS28_23_MULTI
```

Do **not** use only the top-level `dsId` field — compare the full list to FC Details panel DSIDs.

## Manage columns

- Open via table header **gear** (`#tableViewButton`) or Status-area gear (careful — mis-clicks common).
- Saved view for automation: **Aautomation**.
- **Order start date** checkbox is required for BSD-29967 and multi-submission (i) scan.
- **Save changes** stays disabled when no pending edits.

## Left filters

- **Submitted by:** filter shows **full name** (e.g. `Akilandeswari Sundararajan`); grid **Submitted By** column shows **initials** (e.g. `AS`).
- **Environment:** use only options with **non-zero count** for dynamic test data.
- Active filter chip appears under date bar (e.g. `Environment: FSPGLOBAL`).
- Clicking filter-sidebar gear opens **Manage filters** — not Manage columns.

## Details panel

- Tabs: **Details** | Audit Log | Source Details
- Order-level fields at top: Title, Episode, Season, Episode Title, Partner Name, etc.
- **Order details** section has its **own inner vertical scrollbar** (below “Open in Review”). Page-level PageDown does **not** scroll it.
- **Scroll to DSID (manual):** click **Title** or **Episode** in Order details → press **Arrow Down** repeatedly or **Tab** until **DSID** label/values are visible.
- **Automation (BSD-29870):** `DsidColumnUtil.readDsidValuesViaJs()` reads DSIDs from DOM without scrolling (`FF_DSID_BSD29870_ProdServerSuite.xml`).
- Close with **X** or Escape; returns to grid URL without `/focus`.

## BSD-29967 results table template

Copy after each Environment option tested:

```markdown
| Environment | Order ID | Latest job (omfOrderId) | dsIdList (ops-console) | DSIDs (FC Details) | Match |
|-------------|----------|-------------------------|--------------------------|--------------------|-------|
| FSPGLOBAL   | 4590424  | 526061618               | UHDLION302X1, …         | (scroll panel)     | Y/N   |
```

## Evidence to capture

- Screenshot of **(i)** next to Order start date (user reference pattern).
- Details panel with DSID visible.
- Ops-console Dynamo → Jobs → latest → dsIDlist.
- Active filter chip + result count.

## When to update SESSION_NOTES

After every manual session, append dated bullets to
`docs/manual-testing/SESSION_NOTES.md`: new UI quirks, locator hints, data
candidates (order IDs / environments), and story-specific outcomes.
