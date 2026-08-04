# Manual Partner Duplicate Sign-Off Tracker
# Rule: Search COMPLETE exact partner string in Partner filter (Orders tab)
# PASS = exactly 1 visible option with exact same label (count exact matches only)
# FAIL = 2+ rows with same exact label, OR case-variant duplicates visible, OR badge 0/2+ for unique name

**Test date:** 2026-07-15  
**Date range used:** Today (2026-07-14) on both environments  
**Partner master list:** `PartnerNames_FullList.txt` (2,472 names from DEV JSON)  
**Known case pairs:** `PartnerNames_CaseInsensitiveDuplicates.txt` (22 pairs)

---

## Priority 1 — Exact duplicate (reported on PROD)

| Partner Name | PROD Result | DEV Result | Notes |
|--------------|-------------|------------|-------|
| MPD Disney Spain FSP | **FAIL** — Partner 0/2 | **FAIL** — Partner 0/2 | User screenshot: 2 identical rows. DEV fix NOT verified — still 0/2 on both envs |

---

## Priority 2 — User-listed partners

| Partner Name | PROD Result | DEV Result | Notes |
|--------------|-------------|------------|-------|
| Comcast Unified Nick Spanish | **PASS** — 1 exact match (0/3 badge includes FSP substring) | Pending | Substring: Comcast Unified Nick Spanish FSP — not a duplicate |
| Dish/Sling Nick Spanish | **PASS** — 1 exact match (0/2 badge) | Pending | |
| EST MPD APPL FSP US | **PASS** — 1 exact match (0/2 badge) | Pending | |
| EST MPD DTV via Deluxe FSP | Pending | Pending | |
| EST MPD FDG VUDU FSP | Pending | Pending | |
| EST MPD GGL FSP US | Pending | Pending | |

---

## Priority 3 — Case-insensitive pairs (test BOTH strings in each pair)

| Search String | PROD | DEV | Notes |
|---------------|------|-----|-------|
| Amazon LATAM | **FAIL** — 0/5, shows Amazon LATAM + Amazon Latam | Pending | |
| Amazon Latam | Pending | Pending | |
| VUDU | **FAIL** — 0/13, shows VUDU + Vudu | **FAIL** — 0/13, shows VUDU + Vudu | Case duplicate still on DEV |
| Vudu | Pending | **FAIL** — same pair visible | |
| Indemand | **FAIL** — 0/7, shows Indemand + iNDemand | Pending | |
| iNDemand | Pending | Pending | |

*Remaining 19 case pairs: not yet manually tested — see `PartnerNames_CaseInsensitiveDuplicates.txt`*

---

## PROD baseline summary (known issues)

| Issue type | Examples | Status on PROD |
|------------|----------|----------------|
| Exact duplicate (same string × 2) | MPD Disney Spain FSP | FAIL |
| Case-insensitive duplicate | VUDU/Vudu, Amazon LATAM/Amazon Latam, Indemand/iNDemand | FAIL |
| Substring matches (NOT bugs) | Comcast Unified Nick Spanish + Comcast Unified Nick Spanish FSP | PASS (1 exact each) |

**PROD total partners (unfiltered):** ~2,422

---

## DEV fix verification summary

| Check | Expected after fix | Actual on DEV | Verdict |
|-------|------------------|---------------|---------|
| MPD Disney Spain FSP → 1 option | Partner 0/1 | Partner **0/2** | **FAIL** |
| VUDU → single canonical name | 1 exact | VUDU + Vudu both visible | **FAIL** |

---

## Sign-off decision

- [x] PROD baseline documented (priority cases)
- [ ] DEV fix verified — **priority tests still FAIL**
- [ ] Ready for PROD deployment: **NO**

**Recommendation:** Do **not** sign off DEV yet. Ask developer to re-verify:
1. `MPD Disney Spain FSP` still returns Partner **0/2** on DEV (same as PROD).
2. `VUDU` search still shows both `VUDU` and `Vudu` on DEV.

---

## Manual test steps (for you to continue)

1. Open **Orders** tab → expand **Partner** filter.
2. Paste the **complete exact** partner string into the Partner search box.
3. Wait 2–3 seconds for the list to filter; read badge (e.g. `Partner 0/1` = pass for unique name).
4. Count only rows whose label **exactly** matches the search string (ignore longer substring names).
5. Record PROD first, then repeat on DEV.
6. Use **90 days** date range if a partner does not appear under Today-only.

**Test order:** Priority 1 → Priority 2 → all 22 case pairs → optional full list (`PartnerNames_FullList.txt`).
