# Fulfillment Console — PTS Packaging ID Column Test Cases

**Story:** [BSD-29441](https://paramount.atlassian.net/browse/BSD-29441) — FF [Frontend]: Add PTS Packaging ID column to Orders and Line Items tables  
**Backend dependency:** [BSD-29411](https://paramount.atlassian.net/browse/BSD-29411) — `[Backend] Fulfillment - Add PTS packaging ID column` (Complete)  
**Epic:** BSD-29410  
**Component:** Fulfillment Console  
**API field key:** `ptsPackagingId`  
**UI column label:** PTS Packaging ID  
**Label / release:** FF_V1.45.0  
**Reference order (UAT):** `UFCAlchemy210703S`  
**Date:** July 27, 2026

---

## Scope

Validate that **PTS Packaging ID** is available in **Table View** on both **Orders** and **Line Items** tabs, displays correct data from the backend, and supports standard table operations (search, sort, export, Details panel parity).

| Area | Coverage |
|------|----------|
| Table View — column picker | Acceptance criteria |
| Column display & data | Story + BE field mapping |
| Search / sort / export | UAT comment validation |
| Details panel parity | UAT comment validation |

---

## Jira comments (full text)

**Ticket:** [BSD-29441](https://paramount.atlassian.net/browse/BSD-29441) | **Status:** Complete | **Total comments:** 1

### Comment 1 — UAT sign-off

| Field | Detail |
|-------|--------|
| **Author** | Sundararajan, Akilandeswari |
| **Date** | July 21, 2026, 8:55 AM (ET) |
| **Comment ID** | 9232164 |
| **Mentioned** | @Saravanasundaram, M (assignee) |
| **Environment tested** | **UAT** |
| **Tabs tested** | **Orders** and **Line Items** |
| **Reference order** | `UFCAlchemy210703S` |
| **Attachments** | 3 screenshots + 1 screen recording (sort/search/export/Details validation) |

**Comment body:**

> Hi @Saravanasundaram, M
>
> Environment: UAT
>
> Tested the PTS Packaging ID functionality in both the Orders and Line Items tabs.
>
> - PTS Packaging ID column is displayed in the table — **Working**
> - PTS Packaging ID column search — **Working**
> - PTS Packaging ID column sorting — **Working**
> - PTS Packaging ID column values are included correctly in the Excel export — **Working**
> - PTS Packaging ID values displayed in the table match the values shown in the Details panel — **Working**
>
> Order ID UFCAlchemy210703S

### Comment → test case mapping

| # | UAT check (from comment) | Result | Orders test ID | Line Items test ID |
|---|--------------------------|--------|----------------|---------------------|
| 1 | Column displayed in table | Working | FF_PTS_002 | FF_PTS_004 |
| 2 | Column search | Working | FF_PTS_009 | FF_PTS_010 |
| 3 | Column sorting | Working | FF_PTS_011 | FF_PTS_012 |
| 4 | Excel export includes correct values | Working | FF_PTS_013 | FF_PTS_014 |
| 5 | Table value matches Details panel | Working | FF_PTS_015 | FF_PTS_016 |

**Note:** The Jira comment validates **both tabs** for all five checks. Each P1 test above should be executed on **Orders** and **Line Items** separately, using order `UFCAlchemy210703S` as the primary UAT reference data.

---

## Test case summary

| Test ID | Tab | Title | Priority | Type |
|---------|-----|-------|----------|------|
| FF_PTS_001 | Orders | PTS Packaging ID available in Table View column list | P0 | Functional |
| FF_PTS_002 | Orders | Select PTS Packaging ID — column appears in grid | P0 | Functional |
| FF_PTS_003 | Line Items | PTS Packaging ID available in Table View column list | P0 | Functional |
| FF_PTS_004 | Line Items | Select PTS Packaging ID — column appears in grid | P0 | Functional |
| FF_PTS_005 | Orders | Populated PTS Packaging ID values display correctly | P1 | Data |
| FF_PTS_006 | Orders | Empty / null PTS Packaging ID handled gracefully | P2 | Data |
| FF_PTS_007 | Line Items | Populated PTS Packaging ID values display correctly | P1 | Data |
| FF_PTS_008 | Line Items | Empty / null PTS Packaging ID handled gracefully | P2 | Data |
| FF_PTS_009 | Orders | Column search returns matching rows | P1 | Functional |
| FF_PTS_010 | Line Items | Column search returns matching rows | P1 | Functional |
| FF_PTS_011 | Orders | Column sort (asc / desc) works | P1 | Functional |
| FF_PTS_012 | Line Items | Column sort (asc / desc) works | P1 | Functional |
| FF_PTS_013 | Orders | Excel export includes PTS Packaging ID when column selected | P1 | Functional |
| FF_PTS_014 | Line Items | Excel export includes PTS Packaging ID when column selected | P1 | Functional |
| FF_PTS_015 | Orders | Table value matches Details panel | P1 | Data |
| FF_PTS_016 | Line Items | Table value matches Details panel | P1 | Data |
| FF_PTS_017 | Orders | Deselect column removes it from grid | P2 | Functional |
| FF_PTS_018 | Line Items | Deselect column removes it from grid | P2 | Functional |
| FF_PTS_019 | Orders | Saved custom Table View retains PTS Packaging ID selection | P2 | Regression |

---

## Preconditions (all tests)

| Item | Detail |
|------|--------|
| Environment | UAT (primary); DEV / PROD spot-check as needed |
| Access | Valid Fulfillment Console credentials (Okta) |
| Application | Fulfillment Console — Orders / Line Items module |
| Backend | BSD-29411 deployed — `ptsPackagingId` on `Order` and `LineItemViewParentOrder` |
| **Scope filter** | **Demand system = PTS** (PTS Packaging ID applies to PTS orders only) |
| Test data | At least one PTS order with a known PTS Packaging ID (e.g. `UFCAlchemy210703S`) |

---

## P0 — Acceptance criteria

### FF_PTS_001 — Orders: PTS Packaging ID available in Table View

| Field | Detail |
|-------|--------|
| **Tab** | Orders (default landing tab) |
| **Steps** | 1. Log in to Fulfillment Console<br>2. Confirm **Orders** tab is active<br>3. **Left filter → Demand system → select PTS**<br>4. Open **Table View** (manage columns)<br>5. Search or scroll for **PTS Packaging ID** |
| **Expected** | **PTS Packaging ID** appears in the Orders Table View column list (field key `ptsPackagingId`) |

---

### FF_PTS_002 — Orders: Select PTS Packaging ID — column appears in grid

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Jira comment ref** | Comment 1 — "column is displayed in the table — Working" |
| **Steps** | 1. Open Table View on Orders tab<br>2. Enable / check **PTS Packaging ID**<br>3. Apply / close Table View<br>4. Search for order `UFCAlchemy210703S`<br>5. Observe **PTS Packaging ID** column header and cell value |
| **Expected** | **PTS Packaging ID** column header is visible in the Orders table; cell shows populated value for `UFCAlchemy210703S` (UAT: Working) |

---

### FF_PTS_003 — Line Items: PTS Packaging ID available in Table View

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Steps** | 1. Log in to Fulfillment Console<br>2. Navigate to **Line Items** tab<br>3. **Left filter → Demand system → select PTS**<br>4. Open **Table View**<br>5. Search or scroll for **PTS Packaging ID** |
| **Expected** | **PTS Packaging ID** appears in the Line Items Table View column list |

---

### FF_PTS_004 — Line Items: Select PTS Packaging ID — column appears in grid

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Jira comment ref** | Comment 1 — "column is displayed in the table — Working" |
| **Steps** | 1. Open Table View on Line Items tab<br>2. Enable **PTS Packaging ID**<br>3. Apply / close Table View<br>4. Filter line items for order `UFCAlchemy210703S`<br>5. Observe column header and cell values |
| **Expected** | **PTS Packaging ID** column visible on Line Items tab with correct data (UAT: Working) |

---

## P1 — Data display & table operations

### FF_PTS_005 — Orders: Populated PTS Packaging ID values display correctly

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Test data** | Order `UFCAlchemy210703S` (or any order known to have `ptsPackagingId` in BE) |
| **Steps** | 1. Enable **PTS Packaging ID** column on Orders tab<br>2. Search or filter to order `UFCAlchemy210703S`<br>3. Read the **PTS Packaging ID** cell value |
| **Expected** | Cell displays the PTS Packaging ID returned by the API (non-empty for orders that have the field populated); value format matches backend (no truncation or corruption) |

---

### FF_PTS_006 — Orders: Empty / null PTS Packaging ID handled gracefully

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Test data** | Order without `ptsPackagingId` (if available) |
| **Steps** | 1. Enable **PTS Packaging ID** column<br>2. Locate an order with no packaging ID in backend<br>3. Observe the cell |
| **Expected** | Cell is blank or shows consistent empty-state (e.g. `-` / empty); no UI error, broken layout, or `undefined` / `null` text |

---

### FF_PTS_007 — Line Items: Populated PTS Packaging ID values display correctly

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Test data** | Line item linked to order with known PTS Packaging ID |
| **Steps** | 1. Enable **PTS Packaging ID** on Line Items tab<br>2. Filter to a line item under order `UFCAlchemy210703S` (or equivalent)<br>3. Read the cell value |
| **Expected** | PTS Packaging ID displays correctly per `LineItemViewParentOrder.ptsPackagingId` |

---

### FF_PTS_008 — Line Items: Empty / null PTS Packaging ID handled gracefully

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Steps** | 1. Enable **PTS Packaging ID** column<br>2. Locate a line item with no packaging ID<br>3. Observe the cell |
| **Expected** | Empty value handled gracefully; no UI defect |

---

### FF_PTS_009 — Orders: Column search returns matching rows

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Jira comment ref** | Comment 1 — "column search — Working" |
| **Test data** | `UFCAlchemy210703S` |
| **Steps** | 1. Enable **PTS Packaging ID** column on Orders tab<br>2. Note PTS Packaging ID value from row `UFCAlchemy210703S`<br>3. Use column search / filter on **PTS Packaging ID** with that value<br>4. Review filtered results |
| **Expected** | Search returns matching rows only; `UFCAlchemy210703S` appears in results (UAT: Working) |

---

### FF_PTS_010 — Line Items: Column search returns matching rows

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Jira comment ref** | Comment 1 — "column search — Working" |
| **Test data** | Line items under `UFCAlchemy210703S` |
| **Steps** | 1. Enable **PTS Packaging ID** on Line Items tab<br>2. Search using PTS Packaging ID from a line item under `UFCAlchemy210703S` |
| **Expected** | Line Items filtered correctly by PTS Packaging ID (UAT: Working) |

---

### FF_PTS_011 — Orders: Column sort (asc / desc)

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Jira comment ref** | Comment 1 — "column sorting — Working" (see attached video) |
| **Steps** | 1. Enable **PTS Packaging ID** column on Orders tab<br>2. Click column header to sort **ascending** — note order<br>3. Click again to sort **descending** — note order |
| **Expected** | Rows reorder correctly in both directions (UAT: Working) |

---

### FF_PTS_012 — Line Items: Column sort (asc / desc)

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Jira comment ref** | Comment 1 — "column sorting — Working" |
| **Steps** | 1. Enable **PTS Packaging ID** on Line Items tab<br>2. Sort ascending, then descending |
| **Expected** | Sort works correctly on Line Items tab (UAT: Working) |

---

### FF_PTS_013 — Orders: Excel export includes PTS Packaging ID

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Jira comment ref** | Comment 1 — "values included correctly in the Excel export — Working" |
| **Test data** | `UFCAlchemy210703S` |
| **Steps** | 1. Enable **PTS Packaging ID** in Table View on Orders tab<br>2. Filter to `UFCAlchemy210703S` if needed<br>3. Export Orders to Excel<br>4. Open file and compare **PTS Packaging ID** column to UI |
| **Expected** | Export contains **PTS Packaging ID** header and values match grid exactly (UAT: Working) |

---

### FF_PTS_014 — Line Items: Excel export includes PTS Packaging ID

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Jira comment ref** | Comment 1 — "values included correctly in the Excel export — Working" |
| **Steps** | 1. Enable **PTS Packaging ID** on Line Items tab<br>2. Export to Excel<br>3. Verify **PTS Packaging ID** column and values vs UI |
| **Expected** | Export values match UI (UAT: Working) |

---

### FF_PTS_015 — Orders: Table value matches Details panel

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Jira comment ref** | Comment 1 — "values in table match Details panel — Working" |
| **Test data** | `UFCAlchemy210703S` |
| **Steps** | 1. Enable **PTS Packaging ID** column on Orders tab<br>2. Note PTS Packaging ID in table row for `UFCAlchemy210703S`<br>3. Open order **Details** panel<br>4. Compare PTS Packaging ID in Details vs table cell |
| **Expected** | Table and Details values **exactly match** (UAT: Working) |

---

### FF_PTS_016 — Line Items: Table value matches Details panel

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Jira comment ref** | Comment 1 — "values in table match Details panel — Working" |
| **Test data** | Line item under `UFCAlchemy210703S` |
| **Steps** | 1. Enable **PTS Packaging ID** on Line Items tab<br>2. Note value in table for a line item row<br>3. Open line item **Details** panel<br>4. Compare PTS Packaging ID |
| **Expected** | Table and Details values match (UAT: Working) |

---

## P2 — Edge cases & regression

### FF_PTS_017 — Orders: Deselect column removes it from grid

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. With **PTS Packaging ID** visible, open Table View<br>2. Uncheck **PTS Packaging ID**<br>3. Apply changes |
| **Expected** | Column header and cells are removed from Orders grid |

---

### FF_PTS_018 — Line Items: Deselect column removes it from grid

| Field | Detail |
|-------|--------|
| **Tab** | Line Items |
| **Steps** | 1. Uncheck **PTS Packaging ID** in Table View<br>2. Apply |
| **Expected** | Column removed from Line Items grid |

---

### FF_PTS_019 — Orders: Saved Table View retains PTS Packaging ID selection

| Field | Detail |
|-------|--------|
| **Tab** | Orders |
| **Steps** | 1. Enable **PTS Packaging ID** in Table View<br>2. Save as new custom Table View (e.g. `PTS_Packaging_Test_View`)<br>3. Switch to another Table View, then switch back<br>4. Refresh browser and re-select saved view |
| **Expected** | **PTS Packaging ID** remains selected and visible after view switch and page refresh |

---

## Traceability

| Jira | Test IDs |
|------|----------|
| BSD-29441 — AC: Orders Table View selectable | FF_PTS_001, FF_PTS_002 |
| BSD-29441 — AC: Line Items Table View selectable | FF_PTS_003, FF_PTS_004 |
| BSD-29411 — BE field on Order / LineItemViewParentOrder | FF_PTS_005 – FF_PTS_008, FF_PTS_015, FF_PTS_016 |
| UAT sign-off (Jul 21 comment) — display, search, sort, export, Details | FF_PTS_005, FF_PTS_009 – FF_PTS_016 |

---

## UAT execution status (from Jira Comment 1)

All five checks below were executed on **both Orders and Line Items** tabs in **UAT** and marked **Working** by QA on July 21, 2026.

| Check | Result | Orders | Line Items |
|-------|--------|--------|------------|
| Column displayed in table | Working | FF_PTS_002 | FF_PTS_004 |
| Column search | Working | FF_PTS_009 | FF_PTS_010 |
| Column sorting | Working | FF_PTS_011 | FF_PTS_012 |
| Excel export | Working | FF_PTS_013 | FF_PTS_014 |
| Table vs Details panel | Working | FF_PTS_015 | FF_PTS_016 |

**Reference order:** `UFCAlchemy210703S`  
**Evidence:** 3 screenshots + 1 screen recording attached to [BSD-29441](https://paramount.atlassian.net/browse/BSD-29441) Comment 9232164
