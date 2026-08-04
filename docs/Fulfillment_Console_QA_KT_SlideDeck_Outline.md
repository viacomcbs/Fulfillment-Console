# Fulfillment Console — QA Knowledge Transfer
## Slide Deck Outline + Speaker Notes

**Audience:** New QA joiner  
**Duration:** ~90 minutes  
**Presenter:** [Your name]  
**Date:** [Session date]  
**Design reference:** [Fulfillment 2025 — Figma](https://www.figma.com/design/WfLzsbmTLciuNmHsSPyKjU/Fulfillment-2025?node-id=1854-352597)

---

## SLIDE 1 — Title

**On slide:**
- Fulfillment Console — QA Knowledge Transfer
- Operations Console | Paramount MSC
- [Presenter name] | [Date]

**Speaker notes:**
Welcome the new joinee. This session covers what Fulfillment Console is, who uses it, key business rules, and how we test it from a QA perspective. Encourage questions throughout; we’ll leave 10 minutes at the end for Q&A. Share that slides and automation repo paths will be sent after the session.

---

## SLIDE 2 — Agenda

**On slide:**
1. What is Fulfillment Console?
2. Who uses it?
3. Business concepts & rules
4. Application UI walkthrough
5. QA test strategy & environments
6. Automation overview
7. Common defects & sign-off
8. Hands-on next steps & Q&A

**Speaker notes:**
Set expectations: this is QA-focused, not deep backend architecture. Goal is that by end of week 1 they can log in, apply filters confidently, and know where to find test cases and automation. Mention Day-1 checklist will be shared separately.

---

## SLIDE 3 — What Is Fulfillment Console?

**On slide:**
- Internal **Operations Console** web app
- Monitors & manages **fulfillment work orders**
- Tracks delivery of media packages to **downstream partners**
- Used for: search, filter, troubleshoot, assign, restart, export

**Speaker notes:**
Explain in plain language: when Paramount delivers an episode or package to Netflix, Comcast, Amazon, etc., a work order runs through a pipeline. Fulfillment Console is where ops teams **see and act on** those orders. It is **not** a content editing tool or a partner-facing portal. Data comes from backend systems (GraphQL/AppSync, demand systems, VMS). QA’s job is to ensure the UI accurately reflects that data and that filters/actions behave correctly.

---

## SLIDE 4 — What It Is NOT

**On slide:**
- ❌ Not content authoring/editing
- ❌ Not a partner portal
- ❌ Not a generic dashboard
- ✅ Internal ops tool tied to **real pipeline data**

**Speaker notes:**
New testers sometimes assume they can “create orders” freely in DEV — clarify what is read-only vs actionable. Destructive actions (Restart processing, Start delivery, bulk Assign) need care and are usually restricted by role and environment.

---

## SLIDE 5 — Who Uses It?

**On slide:**

| Team | Primary use |
|------|-------------|
| **PFO** | Daily order monitoring, partner delivery tracking |
| **VidOps** | QC & processing for MPX partner deliveries |
| **Fulfillment Ops / MSC** | Search, export, error investigation |
| **QA** | Manual + automated regression |
| **Admin / Super user** | Elevated permissions |

**Speaker notes:**
PFO and VidOps communicate daily using identifiers like **Downstream System ID** to confirm delivered packages. VidOps QC’s files before/at delivery to partners. QA must test with the **correct role** — a read-only user should not see enabled Assign/Restart buttons. Mention security groups: read-only, user, super-user, admin (msc-operations-console-fulfillment-*).

---

## SLIDE 6 — Core Data Hierarchy

**On slide:**
```
Order
 └── Line Item(s)
      └── Package / delivery metadata
```
- **Order** = top-level fulfillment job
- **Line item** = one deliverable within an order
- **Partner** = downstream recipient

**Speaker notes:**
Orders tab = order-level grid. Line items tab = finer granularity (same filters, different columns). Many defects are reported at the wrong level — teach them to note **Orders vs Line items tab** in every bug. Example: “Survivor S3 E306” might appear in both views differently.

---

## SLIDE 7 — Key Business Terms (Glossary)

**On slide:**
- **Partner** — delivery destination (~2,500 options PROD)
- **Series title** — content series (~25,000+ DEV)
- **Environment** — Production, Staging, UAT
- **Job type** — e.g., Fulfillment
- **Downstream System ID** — post-completion package identifier
- **Demand system** — e.g., OPC
- **Delivery Protocol** — e.g., ASPERA

**Speaker notes:**
Don’t expect memorization today — this slide is a reference. Emphasize **Downstream System ID**: generated after work order completion; PFO and VidOps use it to identify delivered packages. Currently in Data in/out modal; new design adds it to line item table and Details panel (see Figma). QA must verify same ID everywhere when feature ships.

---

## SLIDE 8 — Downstream System ID (Design Context)

**On slide:**
- Generated **after work order completes**
- Used by **PFO ↔ VidOps** for MPX partner packages
- Today: **Data in/out modal**
- Planned: **Line item table column** + **Details panel** (ID + name)

**Speaker notes:**
Open Figma link if possible: Fulfillment 2025 prototype. Walk through the three design changes. QA test plan for this feature: ID only when order complete; consistent across table, Details, modal; name matches ID; export includes new field. This is a good example of translating design + business need into test cases.

---

## SLIDE 9 — Business Rule: Date Filtering

**On slide:**
- Date range drives **all filter counts** and **grid results**
- Default views often use **Today** or recent range
- Changing dates → filter badges & result counts change

**Example:** Series title **0/25212** (scoped to active dates)

**Speaker notes:**
Most common new-tester mistake: “I only see 222 Survivor titles” — because date filter is single day. Always document **active date range** in test evidence and bugs. Saved filters can include date presets (Today, Yesterday, Last 7 Days, custom). Verify saved filter restores date + selections.

---

## SLIDE 10 — Business Rule: Status Workflow

**On slide:**
**Order Status (examples):**
- Done: Delivered
- Done: Failed

**Line Item Status (examples):**
- Delivering
- Delivery Complete

**QA rule:** Grid status must match filter selection

**Speaker notes:**
Statuses drive which action buttons are valid. Failed orders often have **Error message** filter values — good for troubleshooting tests. When testing Restart processing or Start delivery, confirm preconditions with a senior QA or ops contact; don’t run on PROD without approval.

---

## SLIDE 11 — Business Rule: Filter Logic

**On slide:**
- Multi-filter = **AND** logic (all must match)
- **Search inside filter** = partial match (large lists)
- **Select All** = all visible options in scope
- **Active filters** = removable chips
- **Clear** = remove all selections
- Large lists use **virtual scroll** (no pagination)

**Speaker notes:**
Demo mentally or live: select Order Status + Partner → grid narrows. Clear removes chips. Partner and Series title use CDK virtual scroll — options load as you scroll. Duplicate-option defect: searching **exact** partner/title string must return **exactly 1 row** — we have a whole test suite for this (DFOC).

---

## SLIDE 12 — Business Rule: Actions & Permissions

**On slide:**

| Action | Requires |
|--------|----------|
| Assign to | Row selected + permission |
| Restart processing | Valid state + permission |
| Start delivery | Valid state + permission |
| Export | Current filtered view |
| Save filter | Modified filters |

**QA rule:** No selection → bulk actions **disabled**

**Speaker notes:**
Test negative cases: nothing selected, read-only user, wrong status. Export should respect current filters and date range. Save filter / Save changes / Clear near top of filter panel — Clear disabled when no filters active.

---

## SLIDE 13 — Application UI Map

**On slide:**
```
┌ Header: FULFILLMENT CONSOLE | AI Search | Alerts | Profile ─┐
├ Filters panel │ Tabs: Orders | Line items | Latest submissions │
│ Saved filters │ Date range | Global search | Active filters    │
│ 21 accordions │ Export | Assign | Restart | Start delivery     │
│               │ Results grid + Details panel                   │
└───────────────┴──────────────────────────────────────────────┘
```

**Speaker notes:**
Live demo starts next section if doing hands-on. Point out global search placeholder: “Search by title, ID or provider.” Details panel opens from row — show order/line item fields. Warning/notification badge on profile is common in DEV.

---

## SLIDE 14 — Main Tabs

**On slide:**
| Tab | Purpose |
|-----|---------|
| **Orders** | Order-level view (default landing) |
| **Line items** | Line-item-level view |
| **Latest submissions** | Recent submission activity |

**Toggle:** Show hidden orders / Show hidden line items

**Speaker notes:**
Orders is default after login — no need to click tab if already there. Line items tab marker in UI: “Line item start date” column or “Show hidden line items.” Cross-tab test: apply filter on Orders, switch to Line items — filters may persist; verify expected behavior (LF_CrossTabSwitchValidationTest in automation).

---

## SLIDE 15 — Left Filter Panel (21 Filters)

**On slide:**
Line Item Status · Order Status · Environment · Job type · Submitted by · **Partner** · **Series title** · Flag · Assigned To · Season number · Region · Episode number · Brand · System Name · Language · Demand system · Error message · Franchise · Delivery Protocol · Activity Type · Content type

**Speaker notes:**
Same 21 filters on Orders and Line items tabs. Three types: **checkbox list** (most), **large list** (Partner, Series title, Episode number), **range** (Season number, Episode number). Large lists need scroll testing and duplicate checks. Partner ~2,425 PROD; Series title ~25k DEV.

---

## SLIDE 16 — Table Views & Columns

**On slide:**
- **Manage columns** → customize grid
- Column groups: Order · Package · Line item
- **Save new view** → personal/team views
- **Standard view** vs custom views

**Speaker notes:**
Automation covers TC_002–TC_005 (manage columns, create view, default selection). QA checks: new view appears in dropdown, toast “Table view created”, columns persist after refresh. Relevant when new fields added (e.g., Downstream System ID column).

---

## SLIDE 17 — Environments

**On slide:**

| Env | URL |
|-----|-----|
| **DEV** | dev-operationsconsole.paramountmsc.com/fulfillment/ |
| **UAT** | uat-operationsconsole.paramountmsc.com/fulfillment/ |
| **PROD** | operationsconsole.paramountmsc.com/fulfillment/ |

**QA rule:** No destructive tests on PROD without approval

**Speaker notes:**
DEV = feature testing, large data. UAT = pre-prod sign-off. PROD = smoke/post-deploy only. PR preview URL exists for specific builds. Always state environment in Jira defects. Credentials via team vault — don’t share passwords in slides.

---

## SLIDE 18 — QA Test Strategy Overview

**On slide:**
1. **Smoke** — login, header, filter panel visible
2. **Left filter regression** — per filter, per category
3. **Duplicate filter options (DFOC)** — pre-deploy gate
4. **Table view** — column management
5. **Story-specific** — from Jira + Figma AC

**Speaker notes:**
Regression is filter-heavy because ops lives in filters daily. DFOC is a data-quality gate we ran for Partner PROD before deploy — found exact duplicates, case variants, search-only duplicates. New features get manual exploratory + targeted automation if stable.

---

## SLIDE 19 — Left Filter Test Categories (Automation)

**On slide:**

| Category | ID | Validates |
|----------|-----|-----------|
| Basic | TC1xx | Expand/collapse, options visible |
| Search | TC2xx | Filter search box |
| Select All | TC4xx | Select/deselect, counts |
| Table sync | TC6xx | Grid matches filter |
| Scroll | TC8xx | Virtual scroll |
| Active filters | TC10xx | Chips |
| Clear | TC11xx | Clear all |

**Speaker notes:**
Naming: LF_O = Orders tab, LF_LI = Line items. Example: LF_O_TC201_Partner_SearchTest. Table sync (TC6xx) catches “filter says X but grid shows Y” — high business impact. Partner skips Select All and Table sync in some configs due to list size — know exceptions.

---

## SLIDE 20 — Duplicate Filter Options (DFOC)

**On slide:**
**Purpose:** Ensure no duplicate labels in filter dropdowns

**Process:**
1. Collect all options (scroll/network)
2. Excel/JSON duplicate analysis
3. Search each exact string → expect **1 result**

**Known issue types:** Exact duplicate · Case variant · Search-only duplicate

**Speaker notes:**
PROD Partner sign-off example: 2,437 raw rows, 2,418 unique, 5 exact duplicate groups, 22 case-insensitive pairs — **NO sign-off** until fixed. Case variants (VUDU/Vudu) may be accepted as separate business decision; **exact duplicates are always bugs**. MPD Disney Spain FSP appeared once in full scroll but twice on search — search-only duplicate, hardest to catch without search validation.

---

## SLIDE 21 — Common Defects to Watch

**On slide:**
- Duplicate filter options
- Filter count vs scroll mismatch
- Date scope confusion
- Virtual scroll gaps / missing options
- Table sync failures
- Wrong tab (Orders vs Line items)
- Action buttons enabled when they shouldn’t be
- Performance/timeouts on large lists

**Speaker notes:**
Share 1–2 real examples from your experience. Teach defect template: Env, URL, tab, date range, filters applied, steps, expected vs actual, screenshot, order/line item ID if available. Attach filter badge screenshot (e.g., Partner 0/2450).

---

## SLIDE 22 — Automation Repo Quick Reference

**On slide:**
**Project:** `FF_Console_Automation`

**Key paths:**
- Suites: `src/test/resources/*.xml`
- Page objects: `pageobjects/`
- Filter helpers: `helpers/leftfilters/`
- DFOC: `tests/duplicatefilteroptionscheck/`

**Run example:**
`mvn test -DsuiteXmlFile=src/test/resources/LeftFilterPerFilter_DevServerSmokeSuite.xml`

**Speaker notes:**
Execution via Synergy (local `localhost:39445/synergy` or Synergy public lab). Local MaxTestTime = 60 min. Large collections (Partner, Series title) need long sessions. TestRail project ID 223 if posting results. Allure reports under `allure-results/`.

---

## SLIDE 23 — Sample Test Case (Template)

**On slide:**
**TC: Partner filter — exact search returns single match**

| Step | Action |
|------|--------|
| 1 | DEV, Orders, date = Today |
| 2 | Expand Partner, search exact name |
| 3 | Count matching rows |

**Pass:** Exactly 1 identical label  
**Fail:** 0 or 2+ identical rows

**Speaker notes:**
Walk through writing this in Jira/TestRail. Emphasize **exact string** for duplicate validation vs partial search for exploratory testing. For Series title with 25k options, use collection + duplicate report then manually verify flagged titles only.

---

## SLIDE 24 — New Joinee: Week 1 Plan

**On slide:**
**Day 1:** DEV login, date filter, one filter, Details panel  
**Day 2–3:** Manual test 3 filters (small, large, range)  
**Day 4–5:** Run smoke suite, read DFOC Partner PROD report  
**Week 2:** Own a story test cycle DEV → UAT

**Speaker notes:**
Assign a buddy for first two weeks. Day 1 hands-on checklist: (1) Login DEV (2) Note date range (3) Expand Order Status (4) Select one status (5) Verify grid (6) Open Details (7) Switch Line items tab (8) Clear filters. Book 30-min follow-up after Day 3.

---

## SLIDE 25 — Access & Resources

**On slide:**
- DEV / UAT credentials → [team vault / lead]
- Synergy setup → [Confluence link / lead]
- Jira project → [project key]
- TestRail → Project 223
- Figma → Fulfillment 2025
- Automation repo → [Git path / URL]

**Speaker notes:**
List actual links your team uses. Confirm new joinee has: Paramount SSO, DEV access, Synergy client, Jira, TestRail, Figma view access, repo clone. Escalation: primary QA lead, ops SME for business rule questions.

---

## SLIDE 26 — Q&A

**On slide:**
- Questions?
- Day-1 checklist & slide deck → shared after session
- Next session: [optional deep-dive topic — DFOC, Synergy, or story walkthrough]

**Speaker notes:**
Common questions: “Can I restart orders in DEV?” (check with lead), “Why do counts differ DEV vs PROD?” (data volume + date), “How long does full Partner collection take?” (~45 min PROD local scroll). Thank them; schedule 1:1 within 48 hours.

---

## SLIDE 27 — Appendix: Order Status Filter Examples (Reference)

**On slide:**
*(Optional — hide if time short)*

Sample values used in automation:
- Done: Delivered
- Done: Failed

**Speaker notes:**
Only use if someone asks for concrete filter values during Q&A. Full list visible in DEV UI — counts change daily.

---

## SLIDE 28 — Appendix: PROD Partner Duplicate Sign-Off Example

**On slide:**
*(Optional — for experienced audience)*

- Raw: 2,437 | Unique: 2,418 | UI badge: 2,425
- 5 exact duplicate groups
- 22 case-insensitive pairs
- **Recommendation: NO sign-off**

**Speaker notes:**
Real example from automation run. Files: PartnerOptions_PROD.json, PartnerNames_PROD_ExactDuplicates.txt. Use to explain why DFOC exists and why QA blocks deploy on filter data quality.

---

# APPENDIX — Copy/Paste for Word

Use each **SLIDE N** block as one section in Word:
- **Heading 1** = Slide title (On slide line)
- **Body** = bullet points under "On slide"
- **Comments / Notes pane** = text under "Speaker notes"

# APPENDIX — Copy/Paste for PowerPoint

1. Create blank presentation ( widescreen 16:9 ).
2. One slide per **SLIDE N** section.
3. Title = first line under "On slide" or slide heading.
4. Content = remaining bullets (keep ≤6 bullets per slide).
5. View → Notes → paste "Speaker notes" for presenter view.

**Suggested theme:** Dark header (Paramount brand if available), minimal text, live DEV demo on Slides 13–15 instead of extra bullets.

---

*End of deck outline*
