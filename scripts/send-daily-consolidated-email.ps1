# Send ONE consolidated email from everything in test-output/daily-aggregate/{date}.
# Merges all *.xml files in the folder (snapshots + testng-results).
#
# Manual copy guide (what to copy where after each batch):
#   docs/test-tracking/Daily-Consolidated-Email-Manual-Guide.md
#
# Usage:
#   .\scripts\send-daily-consolidated-email.ps1
#   .\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-28
#   .\scripts\send-daily-consolidated-email.ps1 -Title "Orders Left Filters - All Batches (PROD)"

param(
    [string] $Date = (Get-Date -Format "yyyy-MM-dd"),

    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [string] $Title = "Fulfillment Console - Daily Automation Summary (PROD)",

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$aggregateDir = "test-output/daily-aggregate/$Date"
$aggregatePath = Join-Path $repoRoot ($aggregateDir -replace "/", [IO.Path]::DirectorySeparatorChar)
$combinedSuite = "src/test/resources/regression/consolidated/Daily_CombinedEmail_ProdServerSuite.xml"

if (-not (Test-Path $aggregatePath)) {
    throw "No daily aggregate folder for $Date. Expected: $aggregatePath"
}

$xmlCount = @(Get-ChildItem $aggregatePath -Filter "*.xml").Count
if ($xmlCount -lt 1) {
    throw "No XML files in $aggregatePath - nothing to consolidate."
}

Write-Host "=== Daily consolidated email ===" -ForegroundColor Green
Write-Host "  Dedupe          : same test class re-run -> keep PASS"
Write-Host "  Excludes        : Synergy infrastructure failures (UnknownHost, session lost, etc.)"
Write-Host "  Date            : $Date"
Write-Host ('  XML files       : {0} file(s) (snapshots + testng-results)' -f $xmlCount)
Write-Host "  Aggregate dir   : $aggregateDir"
Write-Host ('  Stakeholder     : {0} (passed only)' -f $Email)
Write-Host ('  Internal        : {0} (full status)' -f $InternalEmail)
Write-Host ""

if (-not $env:SLACK_WEBHOOK_URL) {
    Write-Warning "SLACK_WEBHOOK_URL is not set - Slack API post will be skipped."
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
    "-Dsystem.test.leftfilteremailsuitetitle=$Title" `
    "-Dsystem.test.aggregateresultsdir=$aggregateDir" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host ('Daily consolidated email sent for {0} with {1} XML files.' -f $Date, $xmlCount) -ForegroundColor Green
