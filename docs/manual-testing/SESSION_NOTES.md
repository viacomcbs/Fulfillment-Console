# Fulfillment Console — Manual Testing Session Notes

Living log of manual QA learnings. Agent skills:

| Skill | Path |
|-------|------|
| Manual testing | `.cursor/skills/fulfillment-console-manual-testing/SKILL.md` |
| Automation | `.cursor/skills/fulfillment-console-automation/SKILL.md` |

This file keeps **dated session evidence**; automation patterns are summarized in the automation skill.

---

## 2026-09-01 — BSD-29967 (multi-submission / DSID) + general FC UI

### Environment & setup

| Item | Value |
|------|-------|
| Story | [BSD-29967](https://paramount.atlassian.net/browse/BSD-29967) |
| FC URL | Dev — `https://dev-operationsconsole.paramountmsc.com/fulfillment/` |
| Date | **Today** `2026-09-01` (single day — **manual only**; automation = Yesterday) |
| Environment filter | **FSPGLOBAL** (first non-zero option; ~726–728 results) |
| Manage columns view | **Aautomation** |
| Order start date column | **Enabled** (required to see (i) icon) |

### Multi-submission (i) icon — visual pattern

User reference (confirmed in Dev grid):

```
Order start date column:
  9/1/2026, 4:18:19 PM  (i)    ← circular info icon immediately right of timestamp
```

- **(i)** = order submitted **multiple times** (BSD-29967 test target).
- Not on every row; scan Order start date column after Environment + **Today** filter (manual).

### Manual vs automation — calendar date

| Mode | BSD-29967 date |
|------|----------------|
| Manual testing | **Today** only |
| Automation | **Yesterday** only (`CalendarSetupUtil.setYesterdayDefaultBookmarkOnce`) |
- On 2026-09-01 / FSPGLOBAL, multiple rows showed `9/1/2026, 6:… PM` with **(i)** visible when Order start date column is on-screen.

### Grid click behavior (important)

| Click target | What opens |
|--------------|------------|
| **Season, Episode, Order start date, Order ID**, etc. (not Status) | **Right Details panel** (`/fulfillment/focus?tableView=orders&order={id}__{env}`) |
| Row **expand chevron** | Inline Package ID / line-items sub-table |
| **Status** column or status chips | Status breakdown — not Details |
| Status header **gear** | Often Manage columns — easy mis-click |

### Orders exercised

| Order ID | Environment | How opened | Notes |
|----------|-------------|------------|-------|
| 3780056 | fspglobal | Click Episode `120` | Single submission; Title **FBI**, S1 E120. Details opened; DSID not in visible viewport without scroll. |
| 4624680 | fspglobal | Click Season `42` | Title **The Challenge**, S42 E4206. Details panel opened. Candidate near multi-submission rows with (i) on Order start date. |

### Ops-console — latest job + dsIdList capture (user reference)

**Reference order:** `4590424` (Lioness S3 E302) — user highlighted fields to capture.

**Steps:**
1. Ops-console → order URL → Workflow = Environment (e.g. FSPGLOBAL)
2. **Dynamo** tab → expand **`jobs`** array
3. If **multiple jobs**, pick **latest** by highest **`lastUpdatedTime`** (or `startTime`)
   - Example: jobs[0] `lastUpdatedTime` 1785349282683 vs jobs[1] **1786295884227** → use **jobs[1]**
4. In the **latest job** object, expand **`dsIdList`** array
5. **Capture every `dsId` value** (with `assetType` for evidence):

| assetType | dsId (example 4590424) |
|-----------|------------------------|
| Promo | HD_ORIGINAL_PPLUS28_23_MULTI |
| Program Segment | UHDLION302X1 |
| Program Segment | UHDLION302X2 |
| Program Segment | UHDLION302X3 |
| Program Segment | UHDLION302X4 |
| Program Segment | UHDLION302X5 |
| Dub Card | UHDLION302DUBSFRFR |
| Parent Program Segment | UHDLION302X |

6. Compare this full list to **all DSID values** in FC Details panel (scroll down in panel).

**Do not** compare only the top-level `dsId` field — use **`dsIdList`** from the **latest job**.

### Details panel scroll — manual technique (user-confirmed 2026-09-01)

The **Order details** section has its **own vertical scrollbar** (inside the yellow-bordered box below “Open in Review”). Page-level PageDown does **not** scroll it.

**Manual steps:**
1. Click **Title** (e.g. *Fire Country*) or **Episode** (e.g. `117`) in the **Order details** section.
2. Press **Arrow Down** repeatedly **or** **Tab** until **DSID** label/values are visible.
3. Record every DSID value (may be multiple rows with copy icons).

**Scriptless recording TC045 (`detailspanel scroll`) — reference order 4114152** (not 4684082):
- URL: `.../focus?tableView=orders&order=4114152__fspglobal`
- Click Title XPath: `//div[2]/div[1]/div[2]/div[1]/div/div/div[2]/span`
- Scroll container CSS: `#details-tab > div > app-details-tab > msc-detail-panel > div > div:nth-of-type(2)`
- Pattern: **ARROW_DOWN** + **SCROLL** on container (y: 60 → 720 in steps) until DSID visible
- DSID section XPath: `//msc-detail-panel/div/div[2]/div[2]/div[2]/div[1]/div/div`
- Example DSIDs captured: `UHDCBSFRC416A1` … `UHDCBSFRC416A5` (order **4114152**, S4 E416)

**Automation (BSD-29870):** `DsidColumnUtil.readDsidValuesViaJs()` reads DSIDs from DOM **without scrolling** (see `FF_DSID_BSD29870_ProdServerSuite.xml`). BSD-29967 should reuse that JS read for all DSID values; optional scroll only if XPath/JS fails.

### Order 4684082 — complete (2026-09-01 session)

| Field | Value |
|-------|-------|
| FC Order ID | **4684082** |
| Environment | fspglobal |
| Partner (ops-console) | PP AMZN FSP LAT |
| Title | Fire Country S1 E117 — *A Cry for Help* |
| Order start date | 9/1/2026, 6:30:20 PM **(i)** |
| Ops-console latest job | **jobs[0] only** — `omfOrderId` **526585018**, `lastUpdatedTime` **1788270895972** |
| FC Details DSIDs | See table below (user scrolled to **Identifiers → DSID**) |
| **Match** | **Y** — all 9 DSIDs match ops-console `dsIdList` |

**FC Details panel DSIDs (Identifiers section):**

| # | dsId |
|---|------|
| 1 | HD_GENERIC_PPLUS20_23_MULTI |
| 2 | UHDCBSFRC117A1 |
| 3 | UHDCBSFRC117A2 |
| 4 | UHDCBSFRC117A3 |
| 5 | UHDCBSFRC117A4 |
| 6 | UHDCBSFRC117A5 |
| 7 | UHDCBSFRC117A6 |
| 8 | UHDCBSFRC117DUBSES419 |
| 9 | UHDCBSFRC117A |

**Ops-console dsIdList (jobs[0], FSPGLOBAL) — same 9 values:**

| assetType | dsId |
|-----------|------|
| Promo | HD_GENERIC_PPLUS20_23_MULTI |
| Program Segment | UHDCBSFRC117A1 |
| Program Segment | UHDCBSFRC117A2 |
| Program Segment | UHDCBSFRC117A3 |
| Program Segment | UHDCBSFRC117A4 |
| Program Segment | UHDCBSFRC117A5 |
| Program Segment | UHDCBSFRC117A6 |
| Dub Card | UHDCBSFRC117DUBSES419 |
| Parent Program Segment | UHDCBSFRC117A |

**BSD-29967 results row:**

| Environment | Order ID | Latest job (omfOrderId) | dsIdList count | Match |
|-------------|----------|-------------------------|----------------|-------|
| FSPGLOBAL | 4684082 | 526585018 | 9 | **Y** |

### Ops-console follow-up (checklist per order)

### Live records on Today

- Result count and status summary chips (**In progress**, **Failed**, etc.) **keep increasing** while the session is open — live ingest on Dev.
- Rows can shift / new rows appear at top — use **Search** with a known **Order ID** after finding a **(i)** row.
- Exact totals are **not** a stable assertion on Today; row-level DSID compare is what matters for BSD-29967.
- This is why **automation uses Yesterday** (stable snapshot); manual stays on Today per story.

- Default embedded viewport ~994px caused mis-clicks; **resize to 1920×1080** helps.
- Left Filters panel consumes width — collapse if table columns clip.
- `#tableViewButton` / gear sometimes has `pointer-events: none` when overlapped — use coordinate click on visible gear or scroll first.
- Filter sidebar gear opens **Manage filters**, not Manage columns.
- Accessibility tree often omits grid cell text — use `browser_search` for column labels (`Order start date`, `9/1/2026`, `PM`).

### Submitted By filter (automation — implemented)

- Filter option: full name (`Akilandeswari Sundararajan`).
- Grid column: initials only (`AS`).
- Helper: `toSubmittedByTableInitials()` in `LeftFilterPanelUtil`.
- Suite: `LF_O_SubmittedBy_All_ProdServerSuite.xml` (8 tests: TC105–1105).

### Automation agent created (2026-09-01)

- Skill: `.cursor/skills/fulfillment-console-automation/SKILL.md`
- Covers: Manage columns utils, left-filter TC categories, Submitted By initials rule, DSID/BSD-29967 blueprint, suite XML patterns, grid click pitfalls.

### BSD-29967 automation spec (2026-09-01)

- **Full point-by-point spec:** `docs/automation/BSD-29967_Automation_Spec.md`
- Includes: manual→automation mapping, (i) icon rules, reuse from BSD-29870, new classes, ops-console flow, pitfalls, implementation checklist.

### Next manual steps

- [x] Order **4684082** — ops-console dsIdList captured (9 items, jobs[0] only).
- [x] FC Details — DSIDs captured under **Identifiers → DSID** (user scroll) — **Match Y**.
- [ ] Repeat for next non-zero Environment options (dynamic names).
- [ ] Implement BSD-29967 automation (reuse `readDsidValuesViaJs()` for all DSIDs).
