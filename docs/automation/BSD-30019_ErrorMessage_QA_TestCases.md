# BSD-30019 — Error message left filter QA test cases

**Category:** In-sprint automation (`src/test/resources/insprint-automation/`)  
**Story:** [BSD-30019](https://paramount.atlassian.net/browse/BSD-30019)  
**Goal:** Validate **Error message** left filter ↔ main grid **Error message(s)** column on **Orders** and **Line items** tabs.  
**Focus:** Error **code** match and count sync (not full message text parity).  
**Automation spec:** `docs/automation/BSD-30019_ErrorMessage_QA_TestCases.md`  
**Suite XMLs (3 Synergy sessions + 1 combined email):**

| Session | File | Tests |
|---------|------|-------|
| 1 — Orders per-filter | `src/test/resources/insprint-automation/BSD-30019_OrdersPerFilter_ProdServerSuite.xml` | 8 |
| 2 — Line Items per-filter | `src/test/resources/insprint-automation/BSD-30019_LineItemsPerFilter_ProdServerSuite.xml` | 8 |
| 3 — Story validation | `src/test/resources/insprint-automation/BSD-30019_StoryValidation_ProdServerSuite.xml` | 6 (Orders + LI) |
| Email only | `src/test/resources/insprint-automation/BSD-30019_SendCombinedEmail_ProdServerSuite.xml` | merges → **1 email** |

Run all (In-sprint automation, default **UAT**): `.\scripts\run-bsd-30019.ps1 -Email "you@paramount.com"`

**Email title:** `BSD-30019 — Error code functionality mismatch between table and left filter`

---

## Preconditions (all cases)

| Item | Value |
|------|--------|
| Environment | **UAT** for automation runs (override with `-Environment PROD` on script); DEV for manual spot-checks |
| URL | `https://dev-operationsconsole.paramountmsc.com/fulfillment/` |
| Tab | Orders **or** Line items (per case) |
| Calendar | Wide enough range to expose failed orders with error codes |
| Status chip | **Failed** (recommended — most error codes appear here) |
| Submission | **Latest submission** + **Recent orders** when testing **[No Value]** |
| Manage columns | Orders: **Error messages** visible · Line items: **Error message** visible |
| Out of scope | Error message column inside **expanded order row** (inline line-items sub-table) — **ignore** |

**Error code samples** (spot-check several prefixes, not one code only):

`UWF_ERROR_100`, `UWFSYS012`, `UWFUSR045`, `ASL407`, `SR500`, `UMTSVAL422`, `AS501`, etc.

**Code format in grid (pass example — Orders, DEV):**  
`(2) UWF_ERROR_100: Video Materials are not configured in EPD for - conten…`  
Primary assertion: cell text **contains the selected filter code** (e.g. `UWF_ERROR_100`, `ASL407`).

---

## Orders view

### TC-O-EM-001 — Single error code: count sync

1. Open **Orders** tab; set calendar + **Failed** status as needed.
2. Expand left filter **Error message**.
3. Pick any option with **count > 0** (not Select All), e.g. `UWF_ERROR_100 (3)`.
4. Note filter option count **N**.
5. Observe table header result count.

**Expected:** Table shows **N results** (same as filter option count).

---

### TC-O-EM-002 — Single error code: table shows code + message

1. Continue from TC-O-EM-001 (one error code selected).
2. For **each visible row**, read **Error messages** column (main grid only).

**Expected:**

- Column shows **selected error code** + descriptive text (e.g. `ASL407: Embedded subtitle template validation failed…`).
- **Pass criterion:** error **code** in column **matches** left filter selection (prefix families: **UWF**, **ASL**, **SR**, **UWFSYS**, **UWFUSR**, **UMTSVAL**, etc.).
- Full message text may truncate — code match is sufficient.

---

### TC-O-EM-003 — Multiple error codes (optional regression)

1. Select **two or more** distinct error codes with non-zero counts.
2. Compare table count to combined filter logic (union of selected codes).

**Expected:** Result count matches filtered set; every row’s **Error messages** cell contains **at least one** of the selected codes.

---

### TC-O-EM-004 — [No Value]: blank or message without code

1. Clear other **Error message** selections.
2. Select only **[No Value]** (UI may show `[No Value]` or `[No value]`).
3. Scan **Error messages** column on all returned rows.

**Expected (either is acceptable):**

- Cell is **blank / empty**, **or**
- Cell shows **message text only** with **no** error code prefix (no `UWF…`, `ASL…`, `SR…` pattern before the message).

**Fail:** Row shows a recognizable error **code** (e.g. `UWF_ERROR_100:` or `ASL407:`) while **[No Value]** filter is the only Error message selection.

**Note:** With **All submissions**, multi-submission orders may appear under **[No Value]** when some submissions have no error — prefer **Latest submission** for this case (per dev thread on ticket).

---

### TC-O-EM-005 — Original defect regression (FILE5635678 / ASL407)

1. Search Order ID **FILE5635678** (PROD reference).
2. Confirm Orders table shows **ASL407** in **Error messages**.
3. Expand **Error message** left filter.

**Expected:** **ASL407** appears as its own filter option (not only under **[No Value]**).

---

## Line items view (order-level grid column)

Same validation as Orders, on the **Line items** tab main grid **Error message** column (not the nested column inside an expanded Orders row).

### TC-LI-EM-001 — Single error code: count sync

Same steps as TC-O-EM-001 on **Line items** tab.

**Expected:** Filter count = table result count.

---

### TC-LI-EM-002 — Single error code: table shows code + message

Same steps as TC-O-EM-002 on **Line items** tab.

**Expected:** **Error message** column includes the selected **error code** (this was the original LI-view gap vs left filter).

---

### TC-LI-EM-003 — [No Value]: blank or message without code

Same steps as TC-O-EM-004 on **Line items** tab.

**Expected:** Blank cell **or** message-only text — **no** error code prefix.

---

### TC-LI-EM-004 — Cross-view consistency (spot-check)

1. On **Orders**, select one error code (e.g. **ASL407**); note count.
2. Switch to **Line items**; select same code.

**Expected:** Counts may differ by view, but for overlapping orders/line items the **same code** appears in the LI grid **Error message** column when that code is selected.

---

## Pass / fail summary

| Area | Pass |
|------|------|
| Count sync | Filter `(N)` = table `N results` |
| Code selected | Grid column contains that **code** |
| [No Value] | No error **code** in column (blank or message-only) |
| LI view | Same as Orders for main grid column |
| Ignore | Expanded-row inner error message column |

---

## Automation note (future)

Existing per-filter suites (`LF_O_TC617`, `LF_LI_TC617`) validate generic table sync only. BSD-30019-specific checks: parse **Error message(s)** column for selected code regex + **[No Value]** negative code check — both tabs.
