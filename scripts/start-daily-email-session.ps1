# Prepare today's multi-run email workflow:
#   - Each run  -> separate email (SendReportAutoEmails=true)
#   - Each run  -> saved to test-output/daily-aggregate/{today}/
#   - End of day -> .\scripts\send-daily-consolidated-email.ps1
#
# Manual copy guide (SkipEmail workflow — copy snapshots before send):
#   docs/test-tracking/Daily-Consolidated-Email-Manual-Guide.md
#
# Usage:
#   .\scripts\start-daily-email-session.ps1
#   .\scripts\start-daily-email-session.ps1 -Reset

param(
    [switch] $Reset,

    [string] $Date = (Get-Date -Format "yyyy-MM-dd")
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$aggregateDir = Join-Path $repoRoot "test-output\daily-aggregate\$Date"

if ($Reset -and (Test-Path $aggregateDir)) {
    Remove-Item -Recurse -Force $aggregateDir
    Write-Host "Cleared $aggregateDir" -ForegroundColor Yellow
}

New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

Write-Host ""
Write-Host "=== Daily email session: $Date ===" -ForegroundColor Green
Write-Host ""
Write-Host "Add these VM options to EACH IntelliJ / Maven run today:" -ForegroundColor Cyan
Write-Host ""
Write-Host @"
-Dsystem.test.appendtodailyaggregate=true
-Dsystem.test.dailyaggregatedir=test-output/daily-aggregate/$Date
-Dsystem.test.sendreportautoemails=true
-Dsystem.test.sendreportemailaddress=Akilandeswari.Sundararajan@paramount.com,qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com
-Dsystem.test.sendreportinternalemailaddress=Akilandeswari.Sundararajan@paramount.com
"@ -ForegroundColor White
Write-Host ""
Write-Host "Per run you get:" -ForegroundColor Cyan
Write-Host "  1) Stakeholder email - passed scenarios only"
Write-Host "  2) Internal email    - full pass/fail status"
Write-Host "  3) Result saved to   - test-output/daily-aggregate/$Date"
Write-Host ""
Write-Host "At end of day run:" -ForegroundColor Cyan
Write-Host "  .\scripts\send-daily-consolidated-email.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Aggregate folder ready: $aggregateDir"
