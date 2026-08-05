# TestRail import — today’s upload checklist

**Project:** Media Platform - Fulfillment Console (223)  
**URL:** https://paramount.testrail.io/index.php?/projects/overview/223

---

## Files to import (in order)

| # | File | Cases | Section created in TestRail |
|---|------|-------|----------------------------|
| 1 | `FF_PTS_Orders_ManualCases_Import.csv` | **7** | Fulfillment Console > BSD-29441 > Orders > PTS Packaging ID |
| 2 | `LF_Orders_TabLevel_ManualCases_Import.csv` | **17** | Fulfillment Console > Left Filters > Orders > Tab Level Regression |
| 3 | `LF_LineItems_TabLevel_ManualCases_Import.csv` | **17** | Fulfillment Console > Left Filters > Line Items > Tab Level Regression |
| 4 | `LF_Orders_PerFilter_ManualCases_Import.csv` | **155** | Fulfillment Console > Left Filters > Orders > Per Filter > … |
| 5 | `LF_LineItems_PerFilter_ManualCases_Import.csv` | **155** | Fulfillment Console > Left Filters > Line Items > Per Filter > … |

**Alternative (single upload):** `LF_LeftFilter_All_ManualCases_Import.csv` — **344** left-filter cases only (no PTS).

**Grand total if all files imported:** 7 + 344 = **351** manual test cases

All files live in: `docs/testrail/`

---

## Import steps in TestRail

1. Open **Test Cases** → **Import** icon (arrow into box, next to “Test Cases” title).
2. Choose **CSV**.
3. Upload one file at a time (recommended for first time).
4. Map columns: **Title, Section, Template, Type, Priority, References, Steps, Expected Result, Automation Type, Automation ID, Preconditions**.
5. Confirm import → verify section tree and case count.

---

## Manual ID conventions

### PTS Packaging ID (Orders) — BSD-29441

| Manual ID | Title |
|-----------|--------|
| FF_PTS_001 | PTS Packaging ID available in Table View |
| FF_PTS_002 | Column visible in grid |
| FF_PTS_009 | Column search |
| FF_PTS_011 | Column sort |
| FF_PTS_013 | Excel export |
| FF_PTS_015 | Table matches Details |
| FF_PTS_017 | Deselect + export excludes |

### Left filters — tab level

| Manual ID | Scope |
|-----------|--------|
| LF_O_001 … LF_O_017 | Orders tab regression |
| LF_LI_001 … LF_LI_017 | Line Items tab regression |

### Left filters — per filter (example: Order Status = filter #2)

| Manual ID | Category |
|-----------|----------|
| LF_O_TC102 | Basic |
| LF_O_TC103 | Option list order |
| LF_O_TC202 | Search |
| LF_O_TC402 | Select all |
| LF_O_TC602 | Table sync |
| LF_O_TC802 | Scroll |
| LF_O_TC1002 | Active filters |
| LF_O_TC1102 | Clear filters |

Same pattern for all **21 filters** × **Orders** and **Line Items** (Partner and Range filters have fewer categories).

---

## After import

1. Note each TestRail case ID (`C#####`) for PTS cases → update `PtsTestRailCaseIds.java`.
2. Link **Automation ID** field (already in CSV) to Git repo when connected.
3. Create a **Test Run** for today’s execution evidence.

---

## Regenerate CSVs

```powershell
python docs/tools/generate_testrail_manual_cases.py
```
