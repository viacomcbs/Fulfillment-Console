# Orders Left Filters — All 5 Batches → One Email (Deferred)

Saved for **Fulfillment Console Automation Agent** (Sep 2026 session).

**Quick manual copy guide (SkipEmail workflow):** `docs/test-tracking/Daily-Consolidated-Email-Manual-Guide.md`

## Goal

Run all **21 Orders left-filter** batches locally on PROD, but send **one consolidated email** (stakeholder = passed only, internal = full pass/fail) at end of day — not one email per batch.

## The 5 batches

| # | Script | Filters | Aggregate snapshot |
|---|--------|---------|-------------------|
| 1 | `scripts/run-lf-orders-2filters-brand-submittedby-prod.ps1` | Brand, Submitted By | `test-output/lf-o-2filter-brand-submittedby-aggregate/lf-recorded-snapshot.xml` |
| 2 | `scripts/run-lf-orders-5filters-workflow-status-prod.ps1` | Order Status, Line Item Status, Activity Type, Job type, Flag | `test-output/lf-o-5filter-workflow-status-aggregate/lf-recorded-snapshot.xml` |
| 3 | `scripts/run-lf-orders-5filters-content-identification-prod.ps1` | Series title, Season number, Episode number, Language, Franchise | `test-output/lf-o-5filter-content-identification-aggregate/lf-recorded-snapshot.xml` |
| 4 | `scripts/run-lf-orders-5filters-environment-routing-prod.ps1` | Environment, Region, System Name, Demand system, Delivery Protocol | `test-output/lf-o-5filter-environment-routing-aggregate/lf-recorded-snapshot.xml` |
| 5 | `scripts/run-lf-orders-5filters-people-distribution-prod.ps1` | Submitted By, Assigned To, Brand, Partner, Content type | `test-output/lf-o-5filter-people-distribution-aggregate/lf-recorded-snapshot.xml` |

**Batch 2** was already run once (Sep 27) — snapshot kept unless you re-run batch 2.

## Key rule

Each batch script sends email **immediately when it finishes** unless you pass **`-SkipEmail`**.

There is **no overnight scheduler** in the repo. One email tomorrow = run batches with `-SkipEmail`, copy snapshots, then run `send-daily-consolidated-email.ps1`.

## Step-by-step

### 0. Prep merge folder (once per day)

```powershell
cd C:\FulfillmentConsole\Fulfillment-Console
.\scripts\start-daily-email-session.ps1 -Date 2026-09-28
```

Creates `test-output/daily-aggregate/2026-09-28/`.

### 1. Run all batches — NO email per batch

```powershell
# First batch: compile once
.\scripts\run-lf-orders-2filters-brand-submittedby-prod.ps1 -SkipEmail

# Remaining batches: skip compile + skip email
.\scripts\run-lf-orders-5filters-workflow-status-prod.ps1 -SkipEmail -SkipCompile
.\scripts\run-lf-orders-5filters-content-identification-prod.ps1 -SkipEmail -SkipCompile
.\scripts\run-lf-orders-5filters-environment-routing-prod.ps1 -SkipEmail -SkipCompile
.\scripts\run-lf-orders-5filters-people-distribution-prod.ps1 -SkipEmail -SkipCompile
```

Skip batch 2 if you already have a fresh snapshot and don't want to re-run (~40–45 min per 5-filter batch; ~2–3+ hours total sequential).

### 2. Copy snapshots into daily aggregate folder

```powershell
$agg = "test-output\daily-aggregate\2026-09-28"
Copy-Item "test-output\lf-o-2filter-brand-submittedby-aggregate\lf-recorded-snapshot.xml" "$agg\batch1-snapshot.xml" -ErrorAction SilentlyContinue
Copy-Item "test-output\lf-o-5filter-workflow-status-aggregate\lf-recorded-snapshot.xml" "$agg\batch2-snapshot.xml" -ErrorAction SilentlyContinue
Copy-Item "test-output\lf-o-5filter-content-identification-aggregate\lf-recorded-snapshot.xml" "$agg\batch3-snapshot.xml" -ErrorAction SilentlyContinue
Copy-Item "test-output\lf-o-5filter-environment-routing-aggregate\lf-recorded-snapshot.xml" "$agg\batch4-snapshot.xml" -ErrorAction SilentlyContinue
Copy-Item "test-output\lf-o-5filter-people-distribution-aggregate\lf-recorded-snapshot.xml" "$agg\batch5-snapshot.xml" -ErrorAction SilentlyContinue
```

### 3. Send ONE consolidated email (afternoon / when all batches done)

```powershell
.\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-28 -Title "Orders Left Filters - All 5 Batches (PROD)"
```

Sends stakeholder (passed only) + internal (full pass/fail) + Slack if `SLACK_WEBHOOK_URL` is set.

### 4. Re-send email only (no re-run)

If batches already ran with `-SkipEmail` and snapshots are copied:

```powershell
.\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-28
```

Per-batch re-send (single batch only):

```powershell
.\scripts\run-lf-orders-5filters-workflow-status-prod.ps1 -EmailOnly -SkipCompile
```

## What is NOT deleted overnight

| Artifact | Behavior |
|----------|----------|
| Transcript logs `test-output/parallel-logs/lf-5filter-*_<timestamp>.log` | Kept (new file per run) |
| Per-batch `lf-recorded-snapshot.xml` | Kept; **overwritten** only if same batch re-run |
| Screenshots / PDF reports | Kept locally; Synergy S3 PDFs ~90 days |
| `allure-results` | Cleared at **start** of next suite run, not overnight |
| Daily aggregate folder | Kept until you delete or `-Reset` on `start-daily-email-session.ps1` |

## Known blockers (Sep 2026)

1. **Batch 3 Manage columns** — UI sometimes shows **Title, Season, Episode** (combined) vs **Title + Season + Episode** (split). Automation must handle both; combined label was failing when only split columns exist.
2. **Batch 2 table sync** — Job type expands order rows and validates line-item Type column (`validateJobTableSyncSmoke` / TC622).
3. **SMTP** — Local runs may timeout on `imailrelay.viacom.com:25`; log says "sent" but inbox may be empty. Use VPN/corporate network.

## Manage columns UI variants (Title / Season / Episode)

PROD **Automation** view can save either layout:

- **Combined:** one column `Title, Season, Episode` (table header + one Manage columns checkbox).
- **Split:** separate `Title`, `Season`, `Episode` columns (three checkboxes).

Batch 3 setup must enable whichever variant is on the view (or normalize to combined before tests).

## Optional: schedule email for fixed time

Windows Task Scheduler (example 2:00 PM):

```powershell
schtasks /create /tn "LF Consolidated Email" /tr "powershell -NoProfile -ExecutionPolicy Bypass -File C:\FulfillmentConsole\Fulfillment-Console\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-28" /sc once /st 14:00 /sd 2026-09-28
```

Only schedule **after** all batches finish and snapshots are copied.
