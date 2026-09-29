# Orders left filters — 48 scenarios (6 Synergy sessions) -> TWO combined emails at end.
# Filters: Brand, Flag, Submitted By, Line Item Status, Activity Type, Order Status (no Job).
#
# Examples:
#   .\scripts\run-lf-core8-48-prod.ps1
#   .\scripts\run-lf-core8-48-prod.ps1 -SkipCompile
#   .\scripts\run-lf-core8-48-prod.ps1 -EmailOnly -SkipCompile

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,
    [switch] $EmailOnly,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\lf-o-core8-48-aggregate"
$sessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"
$combinedSuite = "$sessionsDir/LF_O_Core8_48_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD)"

Write-Host "=== Orders Left Filters (48 scenarios, 6 sessions) -> TWO emails ===" -ForegroundColor Green
Write-Host "Stakeholder email (passed only) : $Email"
Write-Host "Internal email (full status)    : $InternalEmail"
if ($EmailOnly) {
    Write-Host "Mode                            : Email only (reuse test-output/lf-o-core8-48-aggregate)" -ForegroundColor Yellow
}
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

$sessionFiles = @()
$failedRuns = New-Object System.Collections.Generic.List[string]

if (-not $EmailOnly) {
    if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

    $sessionFiles = Get-ChildItem -Path (Join-Path $repoRoot $sessionsDir) -Filter "LF_O_Core8_48_S*.xml" |
        Where-Object { $_.Name -notmatch "CombinedEmail" } |
        Sort-Object Name
    if ($sessionFiles.Count -eq 0) {
        throw "No session suite XMLs found (LF_O_Core8_48_S*.xml under $sessionsDir)"
    }

    $sessionNum = 0
    foreach ($sessionFile in $sessionFiles) {
        $sessionNum++
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "LF session $sessionNum/$($sessionFiles.Count): $($sessionFile.Name)" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan

        $relSuite = $sessionFile.FullName.Substring($repoRoot.Length + 1) -replace "\\", "/"
        mvn -B test `
            "-DsuiteXmlFile=$relSuite" `
            "-Dsystem.test.testenvironment=PROD" `
            "-Dsystem.test.sendreportautoemails=false" `
            "-Dsystem.test.sendchatreport=false" `
            "-Dsystem.test.userkey=$akilaUserKey" `
            "-Dsystem.test.clientid=$akilaClientId"

        $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
        if (Test-Path $resultsFile) {
            Copy-Item $resultsFile (Join-Path $aggregateDir "$($sessionFile.BaseName)-testng-results.xml") -Force
        } else {
            Write-Warning "Missing testng-results.xml after $($sessionFile.Name)"
        }

        if ($LASTEXITCODE -ne 0) {
            $failedRuns.Add($sessionFile.Name) | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run full script first (without -EmailOnly)"
    }
    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    if ($xmlCount -lt 1) {
        throw "No result XML files in $aggregateDir"
    }
    Write-Host "Skipping Synergy - re-sending emails from $xmlCount saved result file(s)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Sending TWO combined LEFT FILTER emails ===" -ForegroundColor Yellow
Write-Host "  1) Stakeholder - passed scenarios only" -ForegroundColor Cyan
Write-Host "  2) Internal    - full pass/fail status" -ForegroundColor Cyan
if (-not $env:SLACK_WEBHOOK_URL) {
    Write-Warning "SLACK_WEBHOOK_URL is not set - Slack API post will be skipped (Slack email-in still works)."
}
$slackArgs = @()
if ($env:SLACK_WEBHOOK_URL) {
    $slackArgs += "-Dsystem.test.slackwebhookurl=$($env:SLACK_WEBHOOK_URL)"
}

mvn -B test `
    "-DsuiteXmlFile=$combinedSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-core8-48-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done."
if (-not $EmailOnly) {
    Write-Host "  Sessions run       : $($sessionFiles.Count)"
}
Write-Host "  Aggregate dir      : test-output/lf-o-core8-48-aggregate"
Write-Host "  Report title       : $reportTitle"
Write-Host "  Stakeholder email  : $Email (passed only)"
Write-Host "  Internal email     : $InternalEmail (full status)"
if ($failedRuns.Count -gt 0) {
    Write-Host ""
    Write-Host "Sessions with Maven failures (partial results still merged):" -ForegroundColor Yellow
    foreach ($name in $failedRuns) {
        Write-Host "  - $name" -ForegroundColor Yellow
    }
}
