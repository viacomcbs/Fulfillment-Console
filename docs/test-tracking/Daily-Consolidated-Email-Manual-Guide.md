# Daily Consolidated Email — Manual Copy Guide

Saved: 2026-09-28. Use when batches were run with `-SkipEmail` and you need **one end-of-day email** without re-running tests.

**IntelliJ console logs are not used for the email.** Results live in `lf-recorded-snapshot.xml` files on disk. You can close IntelliJ after batches finish.

---

## 1. Merge folder (one per day)

All XML copied here is merged into a single email:

```
C:\FulfillmentConsole\Fulfillment-Console\test-output\daily-aggregate\{YYYY-MM-DD}
```

Example for today:

```
test-output\daily-aggregate\2026-09-28
```

Create the folder once (optional — copy commands work if it already exists):

```powershell
cd C:\FulfillmentConsole\Fulfillment-Console
.\scripts\start-daily-email-session.ps1
# or: .\scripts\start-daily-email-session.ps1 -Date 2026-09-28
```

---

## 2. After each batch — copy snapshot INTO merge folder

Run from repo root. Replace `{date}` with today (`2026-09-28`).

```powershell
cd C:\FulfillmentConsole\Fulfillment-Console
$agg = "test-output\daily-aggregate\{date}"
New-Item -ItemType Directory -Force -Path $agg | Out-Null
```

| Batch | Filters | Copy **FROM** | Copy **TO** (in `$agg`) |
|-------|---------|---------------|-------------------------|
| 1 | Brand, Submitted By | `test-output\lf-o-2filter-brand-submittedby-aggregate\lf-recorded-snapshot.xml` | `batch1-brand-submittedby-snapshot.xml` |
| 2 | Order Status, Line Item Status, Activity Type, Job type, Flag | `test-output\lf-o-5filter-workflow-status-aggregate\lf-recorded-snapshot.xml` | `batch2-workflow-status-snapshot.xml` |
| 3 | Series title, Season number, Episode number, Language, Franchise | `test-output\lf-o-5filter-content-identification-aggregate\lf-recorded-snapshot.xml` | `batch3-content-identification-snapshot.xml` |
| 4 | Environment, Region, System Name, Demand system, Delivery Protocol | `test-output\lf-o-5filter-environment-routing-aggregate\lf-recorded-snapshot.xml` | `batch4-environment-routing-snapshot.xml` |
| 5 | Submitted By, Assigned To, Brand, Partner, Content type | `test-output\lf-o-5filter-people-distribution-aggregate\lf-recorded-snapshot.xml` | `batch5-people-distribution-snapshot.xml` |
| Extra | Line Item Status (+ Error message) | `test-output\lf-o-2filter-lineitemstatus-errormessage-aggregate\lf-recorded-snapshot.xml` | `batch-lineitemstatus-errormessage-snapshot.xml` |

**All copy commands (run only for batches you actually ran today):**

```powershell
Copy-Item "test-output\lf-o-2filter-brand-submittedby-aggregate\lf-recorded-snapshot.xml" "$agg\batch1-brand-submittedby-snapshot.xml" -Force
Copy-Item "test-output\lf-o-5filter-workflow-status-aggregate\lf-recorded-snapshot.xml" "$agg\batch2-workflow-status-snapshot.xml" -Force
Copy-Item "test-output\lf-o-5filter-content-identification-aggregate\lf-recorded-snapshot.xml" "$agg\batch3-content-identification-snapshot.xml" -Force
Copy-Item "test-output\lf-o-5filter-environment-routing-aggregate\lf-recorded-snapshot.xml" "$agg\batch4-environment-routing-snapshot.xml" -Force
Copy-Item "test-output\lf-o-5filter-people-distribution-aggregate\lf-recorded-snapshot.xml" "$agg\batch5-people-distribution-snapshot.xml" -Force
Copy-Item "test-output\lf-o-2filter-lineitemstatus-errormessage-aggregate\lf-recorded-snapshot.xml" "$agg\batch-lineitemstatus-errormessage-snapshot.xml" -Force
```

Only copy rows for batches you ran. Skip missing source files.

---

## 3. Optional cleanup (avoid double-counting Environment)

If Environment appears twice in the email, remove duplicate XML in the merge folder:

```powershell
Remove-Item "$agg\lf-recorded-snapshot.xml" -ErrorAction SilentlyContinue
Remove-Item "$agg\LF_O_EnvironmentRouting-run*.xml" -ErrorAction SilentlyContinue
```

Keep `batch4-environment-routing-snapshot.xml` — that is the canonical Environment batch snapshot.

---

## 4. Verify before sending

```powershell
Get-ChildItem "$agg\*.xml" | Select-Object Name, LastWriteTime
```

Expected for a full day (example Sep 28 run):

- `batch1-brand-submittedby-snapshot.xml`
- `batch2-workflow-status-snapshot.xml`
- `batch3-content-identification-snapshot.xml` ← Series / Season / Episode / Language / Franchise
- `batch4-environment-routing-snapshot.xml`
- `batch-lineitemstatus-errormessage-snapshot.xml` (if that batch was run)

---

## 5. Send ONE consolidated email (PowerShell — not IntelliJ)

```powershell
cd C:\FulfillmentConsole\Fulfillment-Console
.\scripts\send-daily-consolidated-email.ps1 -Date {date} -Title "Orders Left Filters (PROD) - All batches today"
```

Example:

```powershell
.\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-28 -Title "Orders Left Filters (PROD) - All batches today"
```

Sends:

- **Stakeholder** → passed scenarios only (`Akilandeswari.Sundararajan@paramount.com` + Slack email)
- **Internal** → full pass/fail (`Akilandeswari.Sundararajan@paramount.com`)

**VPN:** Connect to corporate network if SMTP to `imailrelay.viacom.com:25` times out.

PDF reports are also written under `test-output\reports\` and uploaded to Synergy S3 when the send job runs.

---

## 6. Re-send email only (no re-run, no re-copy)

If snapshots are already in `daily-aggregate\{date}`:

```powershell
.\scripts\send-daily-consolidated-email.ps1 -Date {date}
```

---

## 7. Daily workflow (cheat sheet)

1. Run each batch with **`-SkipEmail`** (and `-SkipCompile` after the first batch).
2. After **each** batch finishes → copy its `lf-recorded-snapshot.xml` into `test-output\daily-aggregate\{today}\` (table above).
3. End of day → run `send-daily-consolidated-email.ps1` once.

Batch run scripts (for reference):

| Script |
|--------|
| `scripts/run-lf-orders-2filters-brand-submittedby-prod.ps1` |
| `scripts/run-lf-orders-5filters-workflow-status-prod.ps1` |
| `scripts/run-lf-orders-5filters-content-identification-prod.ps1` |
| `scripts/run-lf-orders-5filters-environment-routing-prod.ps1` |
| `scripts/run-lf-orders-5filters-people-distribution-prod.ps1` |
| `scripts/run-lf-orders-2filters-lineitemstatus-errormessage-prod.ps1` |

---

## Related docs

- Full runbook: `.cursor/skills/fulfillment-console-automation/references/LF_Orders_5Batch_Deferred_Email_Runbook.md`
- Scripts: `scripts/start-daily-email-session.ps1`, `scripts/send-daily-consolidated-email.ps1`
