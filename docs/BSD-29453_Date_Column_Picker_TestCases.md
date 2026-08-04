# Fulfillment Console — Date Column Header Picker Test Cases

**Story:** [BSD-29453](https://paramount.atlassian.net/browse/BSD-29453) — [Frontend] Fulfillment — UI support for adding date pickers on the headers for date columns (Orders & Line Items)  
**Component:** Fulfillment Console  
**Feature flag:** `enable-date-pickers-on-date-columns`  
**Label / release:** FF_V1.45.0  
**Status:** Complete (PR merged DEV Jul 20, 2026)  
**Date:** July 31, 2026

---

## Scope

Validate **column-level date pickers** on date-type table headers on **Orders** and **Line Items** tabs.

### Main calendar vs column picker (important)

| Layer | Location | Role |
|-------|----------|------|
| **Main calendar** | Toolbar (`app-custom-date-range-picker` / calendar icon top of page) | **Global scope** — drives which orders/line items are loaded, left-filter counts, and base grid results |
| **Column date picker** | Date column header (filter/calendar icon in grid header) | **Subset filter** — narrows the **already loaded** grid by that **specific date column** value |

**QA rule:** Always set the **main calendar first** (e.g. Yesterday, Last 7 Days, or a custom range with known data). Column pickers filter **within** that main range — they do **not** replace the main calendar.

---

## In-scope date columns

| Tab | Column | Notes |
|-----|--------|-------|
| **Orders** | Order Start date | |
| **Orders** | Order End date | |
| **Orders** | Last Updated | Used in [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) repro |
| **Orders** | Partner Go live | Used in [BSD-29534](https://paramount.atlassian.net/browse/BSD-29534) repro |
| **Orders** | Partner end date | |
| **Line Items** | Order Start date | |
| **Line Items** | Order End date | |
| **Line Items** | Last Updated | |
| **Line Items** | Start time | Date/time column — verify picker behaviour |

Non-date columns (e.g. Partner, Order Status, PTS Packaging ID) must **not** show a date picker icon.

---

## Known defects (regression watch)

| Bug | Summary | Test IDs |
|-----|---------|----------|
| [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) | Left filter options blank after column date filter applied | FF_DCP_015, FF_DCP_016 |
| [BSD-29534](https://paramount.atlassian.net/browse/BSD-29534) | Partner Go live picker returns rows outside selected range | FF_DCP_010 |

---

## Test case summary

| Test ID | Tab | Title | Priority | Type |
|---------|-----|-------|----------|------|
| FF_DCP_001 | Orders | Date picker icon visible on all in-scope date columns | P0 | UI |
| FF_DCP_002 | Line Items | Date picker icon visible on all in-scope date columns | P0 | UI |
| FF_DCP_003 | Orders | Non-date columns do not show date picker icon | P0 | UI |
| FF_DCP_004 | Orders | Column picker opens from header and shows date range UI | P0 | Functional |
| FF_DCP_005 | Line Items | Column picker opens from header and shows date range UI | P0 | Functional |
| FF_DCP_006 | Orders | Apply column filter — grid updates within main calendar scope | P1 | Functional |
| FF_DCP_007 | Line Items | Apply column filter — grid updates within main calendar scope | P1 | Functional |
| FF_DCP_008 | Orders | Last Updated column filter — cell dates within selected range | P1 | Data |
| FF_DCP_009 | Orders | Order Start date column filter — cell dates within selected range | P1 | Data |
| FF_DCP_010 | Orders | Partner Go live column filter — cell dates within selected range | P1 | Data |
| FF_DCP_011 | Orders | Partner end date column filter — cell dates within selected range | P1 | Data |
| FF_DCP_012 | Line Items | Last Updated column filter — cell dates within selected range | P1 | Data |
| FF_DCP_013 | Both | Clear / remove column date filter restores previous grid | P1 | Functional |
| FF_DCP_014 | Both | Main calendar change resets or updates column filter state correctly | P1 | Functional |
| FF_DCP_015 | Orders | Left filter panel remains populated after column date filter | P1 | Regression |
| FF_DCP_016 | Line Items | Left filter panel remains populated after column date filter | P1 | Regression |
| FF_DCP_017 | Orders | Column date filter + left filter (AND) narrows results correctly | P1 | Functional |
| FF_DCP_018 | Both | Active filters chip reflects column date filter | P2 | UI |
| FF_DCP_019 | Both | Two column date filters applied together (if supported) | P2 | Functional |
| FF_DCP_020 | Both | Sort on date column still works with column filter active | P2 | Functional |
| FF_DCP_021 | Both | Excel export respects column date filter | P2 | Functional |
| FF_DCP_022 | Both | Switch Orders ↔ Line Items — column filter behaviour | P2 | Functional |
| FF_DCP_023 | Both | Feature flag off — no column date picker icons | P2 | Config |

---

## Preconditions (all tests)

| Item | Detail |
|------|--------|
| Environment | DEV (primary for new automation); UAT / PROD spot-check |
| URL | `https://dev-operationsconsole.paramountmsc.com/fulfillment/` (DEV) |
| Access | Valid Fulfillment Console credentials (Okta) |
| Feature flag | `enable-date-pickers-on-date-columns` = **ON** (except FF_DCP_023) |
| **Main calendar** | Set **before** any column picker test — e.g. **Yesterday** (mark as default) or **Last 7 Days** with known data |
| Table View | Enable required date columns via **Manage columns** if not visible by default |
| Left filter panel | Expanded; confirm filter options load (Partner, Order Status, etc.) before column filter |

**Recommended main calendar setup (manual):**
1. Click toolbar calendar icon  
2. Select **Yesterday** (or wider range with test data)  
3. Optionally **bookmark / set as default** so later tests skip main calendar  
4. Confirm toolbar shows start/end dates and grid loads records  

---

## P0 — Acceptance criteria

### FF_DCP_001 — Orders: Date picker icon on all date columns

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Precondition** | Main calendar set; date columns visible in grid |
| **Steps** | 1. Log in → Orders tab<br>2. Set main calendar (e.g. Yesterday)<br>3. Enable via Manage columns: Order Start date, Order End date, Last Updated, Partner Go live, Partner end date<br>4. Inspect each column header |
| **Expected** | Each listed date column header shows a **date picker / filter icon** (distinct from sort-only behaviour on non-date columns) |

---

### FF_DCP_002 — Line Items: Date picker icon on all date columns

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Steps** | 1. Navigate to Line Items tab<br>2. Set main calendar if not already set<br>3. Enable: Order Start date, Order End date, Last Updated, Start time<br>4. Inspect each column header |
| **Expected** | Date picker icon present on all four Line Items date columns |

---

### FF_DCP_003 — Orders: Non-date columns have no date picker

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. Enable non-date columns (e.g. Order ID, Partner, Order Status)<br>2. Inspect headers |
| **Expected** | No date picker/filter calendar icon on non-date columns |

---

### FF_DCP_004 — Orders: Column picker opens and shows date UI

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Column** | Last Updated (or any date column) |
| **Steps** | 1. Main calendar = Yesterday (or known range)<br>2. Click **date picker icon** on **Last Updated** header<br>3. Observe popup/panel |
| **Expected** | Date range picker opens (start/end inputs or calendar UI); user can select a range **within or equal to** the effective data scope; Apply/Clear or equivalent actions available |

---

### FF_DCP_005 — Line Items: Column picker opens and shows date UI

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Column** | Last Updated |
| **Steps** | Same as FF_DCP_004 on Line Items tab |
| **Expected** | Column date picker opens with range selection UI |

---

## P1 — Core functional & data validation

### FF_DCP_006 — Orders: Column filter narrows grid (within main calendar)

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. Main calendar = **07/14/2026 – 07/21/2026** (or DEV equivalent with data)<br>2. Note total result count<br>3. Open **Last Updated** column picker → select sub-range **07/18/2026 – 07/20/2026** → Apply<br>4. Compare result count and sample rows |
| **Expected** | Grid count **decreases or stays same** (never increases beyond main calendar scope); all visible rows have Last Updated within the **column** selected range |

---

### FF_DCP_007 — Line Items: Column filter narrows grid (within main calendar)

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Steps** | Same pattern as FF_DCP_006 using Line Items **Last Updated** |
| **Expected** | Grid filtered by column range; rows still respect main calendar boundary |

---

### FF_DCP_008 — Orders: Last Updated — values match filter range

| Field | Detail |
|-------|--------|
| **Regression** | [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) context |
| **Steps** | 1. Apply Last Updated column filter (e.g. 07/14/2026 – 07/21/2026)<br>2. Scan first 10–20 visible rows<br>3. Compare each **Last Updated** cell to selected range |
| **Expected** | **Every** visible row’s Last Updated date falls within the applied column range (inclusive) |

---

### FF_DCP_009 — Orders: Order Start date — values match filter range

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply Order Start date column filter for a bounded range<br>2. Verify Order Start date cells in visible rows |
| **Expected** | All visible Order Start dates within selected range |

---

### FF_DCP_010 — Orders: Partner Go live — values match filter range

| Field | Detail |
|-------|--------|
| **Regression** | [BSD-29534](https://paramount.atlassian.net/browse/BSD-29534) |
| **Steps** | 1. Main calendar = range with Partner Go live data<br>2. Enable **Partner Go live** column<br>3. Apply column filter (bounded range)<br>4. Review **every visible row** Partner Go live value vs selected range |
| **Expected** | **No** orders with Partner Go live **outside** the selected column range; empty cells handled consistently (document behaviour) |

---

### FF_DCP_011 — Orders: Partner end date — values match filter range

| Field | Detail |
|-------|--------|
| **Steps** | Apply Partner end date column filter; verify cell values |
| **Expected** | All visible Partner end dates within selected range |

---

### FF_DCP_012 — Line Items: Last Updated — values match filter range

| Field | Detail |
|-------|--------|
| **Steps** | Apply Last Updated filter on Line Items; verify cells |
| **Expected** | All visible Last Updated values within column filter range |

---

### FF_DCP_013 — Clear column date filter restores grid

| Field | Detail |
|-------|--------|
| **Tab** | Orders and Line Items |
| **Steps** | 1. Note grid count with **main calendar only**<br>2. Apply column date filter → note reduced count<br>3. Clear column filter (picker Clear, header reset, or Active filters chip remove)<br>4. Compare grid to step 1 |
| **Expected** | Grid returns to pre-column-filter state; main calendar unchanged |

---

### FF_DCP_014 — Main calendar change vs column filter

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. Main calendar = Range A; apply column filter on Last Updated<br>2. Change **main calendar** to Range B<br>3. Observe column filter indicator and grid |
| **Expected** | Document actual behaviour: column filter clears **or** re-scopes to new main range; grid never shows data outside **main calendar**; no stale/orphan filters without user indication |

---

### FF_DCP_015 — Orders: Left filters stay populated after column filter (BSD-29511)

| Field | Detail |
|-------|--------|
| **Regression** | [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) |
| **Steps** | 1. Expand left filter panel — confirm Partner, Episode number, Order Status lists show options with counts<br>2. Apply **Last Updated** column filter (07/14/2026 – 07/21/2026)<br>3. Re-expand left filters (Partner, Order Status, Episode number)<br>4. Attempt to select a left filter value |
| **Expected** | Left filter **option lists remain populated** (not blank); counts reflect current result set; user can refine with left filters; grid may show N results and filters show scoped counts (e.g. Partner x/y — **not** 0/y with empty list) |

---

### FF_DCP_016 — Line Items: Left filters stay populated after column filter

| Field | Detail |
|-------|--------|
| **Regression** | [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) |
| **Steps** | Repeat FF_DCP_015 on **Line Items** tab |
| **Expected** | Left filter options remain visible and selectable after column date filter |

---

### FF_DCP_017 — Column filter + left filter (AND logic)

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. Main calendar set<br>2. Apply Last Updated column filter<br>3. Select one **Order Status** in left filter<br>4. Verify grid |
| **Expected** | Results match **both** column date filter **and** left filter (AND); counts update on both toolbar and left panel |

---

## P2 — Extended coverage

### FF_DCP_018 — Active filters chip for column date filter

| Field | Detail |
|-------|--------|
| **Steps** | Apply column date filter → check **Active filters** area |
| **Expected** | Column filter appears as removable chip/indicator; removing it clears column filter |

---

### FF_DCP_019 — Multiple column date filters

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply **Order Start date** column filter<br>2. Apply **Last Updated** column filter<br>3. Verify grid |
| **Expected** | Document behaviour: both apply (AND) or second replaces first — grid consistent; no errors |

---

### FF_DCP_020 — Sort with column filter active

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply column date filter<br>2. Sort same column ascending / descending |
| **Expected** | Sort works; filtered rows remain filtered; order changes correctly |

---

### FF_DCP_021 — Export respects column date filter

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply Last Updated column filter<br>2. Export Orders (or Line Items)<br>3. Open Excel — check Last Updated column |
| **Expected** | Export contains only rows matching current view (main calendar + column filter) |

---

### FF_DCP_022 — Tab switch behaviour

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply column filter on Orders<br>2. Switch to Line Items<br>3. Switch back to Orders |
| **Expected** | Document whether column filter persists per tab; no cross-tab leakage; main calendar persists |

---

### FF_DCP_023 — Feature flag off

| Field | Detail |
|-------|--------|
| **Precondition** | `enable-date-pickers-on-date-columns` = **OFF** |
| **Steps** | Open Orders and Line Items; inspect date column headers |
| **Expected** | **No** column-level date picker icons; main calendar still works; legacy column sort/search unchanged |

---

## Manual execution checklist (quick pass)

Use this order for first DEV manual run:

| # | Action | Pass? |
|---|--------|-------|
| 1 | Login DEV; set **main calendar** (Yesterday + default bookmark) | |
| 2 | FF_DCP_001 — Orders date column icons | |
| 3 | FF_DCP_004 + FF_DCP_008 — Last Updated open + filter accuracy | |
| 4 | FF_DCP_015 — Left filters after column filter (**BSD-29511**) | |
| 5 | FF_DCP_010 — Partner Go live accuracy (**BSD-29534**) | |
| 6 | FF_DCP_013 — Clear column filter | |
| 7 | FF_DCP_002 + FF_DCP_005 + FF_DCP_012 — Line Items smoke | |
| 8 | FF_DCP_016 — Line Items left filter regression | |

---

## Automation mapping (future)

| Manual ID | Suggested automation class prefix | Notes |
|-----------|-----------------------------------|-------|
| FF_DCP_001–003 | `FF_DCP_O_001` … | Header icon visibility |
| FF_DCP_004–011 | `FF_DCP_O_00x` / `FF_DCP_LI_00x` | Reuse `CalendarSetupUtil` for **main** calendar once per suite |
| FF_DCP_015–016 | `FF_DCP_O_015` / `FF_DCP_LI_016` | Left filter option count assertions |
| FF_DCP_010 | `FF_DCP_O_010` | Row-by-row date validation |

**Automation precondition:** Mirror PTS suite — set main calendar **once** (Yesterday + default); column picker tests only interact with **header** pickers, not main calendar again.

---

## References

- Story: [BSD-29453](https://paramount.atlassian.net/browse/BSD-29453)
- Bug: [BSD-29511](https://paramount.atlassian.net/browse/BSD-29511) — Left filter blank after column date filter
- Bug: [BSD-29534](https://paramount.atlassian.net/browse/BSD-29534) — Partner Go live range accuracy
- Related QA KT: Main calendar drives filter counts (`docs/Fulfillment_Console_QA_KT_SlideDeck_Outline.md` — Slide 9)
