# Send ONE consolidated email for all runs saved today under test-output/daily-aggregate/{date}.
#
# Usage:
#   .\scripts\send-daily-consolidated-email.ps1
#   .\scripts\send-daily-consolidated-email.ps1 -Date 2026-09-25
#   .\scripts\send-daily-consolidated-email.ps1 -Title "Partner + TC406 reruns"

param(
    [string] $Date = (Get-Date -Format "yyyy-MM-dd"),

    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "Vishnupriya.Arumugam@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "Vishnupriya.Arumugam@paramount.com"
    ),

    [string] $Title = "Fulfillment Console — Daily Automation Summary (PROD)",

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$aggregateDir = "test-output/daily-aggregate/$Date"
$aggregatePath = Join-Path $repoRoot ($aggregateDir -replace "/", [IO.Path]::DirectorySeparatorChar)
$combinedSuite = "src/test/resources/regression/consolidated/Daily_CombinedEmail_ProdServerSuite.xml"

if (-not (Test-Path $aggregatePath)) {
    throw "No daily aggregate folder for $Date — run tests with AppendToDailyAggregate=true first. Expected: $aggregatePath"
}

$xmlCount = @(Get-ChildItem $aggregatePath -Filter "*-testng-results.xml").Count
if ($xmlCount -lt 1) {
    throw "No saved run files in $aggregatePath — nothing to consolidate."
}

Write-Host "=== Daily consolidated email ===" -ForegroundColor Green
Write-Host "  Dedupe          : same test-case id -> keep PASS if re-run"
Write-Host "  Excludes        : Synergy infrastructure failures (UnknownHost, session lost, etc.)"
Write-Host "  Date            : $Date"
Write-Host "  Saved runs      : $xmlCount file(s)"
Write-Host "  Aggregate dir   : $aggregateDir"
Write-Host "  Stakeholder     : $Email (passed only)"
Write-Host "  Internal        : $InternalEmail (full status)"
Write-Host ""

if (-not $env:SLACK_WEBHOOK_URL) {
    Write-Warning "SLACK_WEBHOOK_URL is not set — Slack API post will be skipped."
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
Write-Host "Daily consolidated email sent for $Date ($xmlCount run file(s))." -ForegroundColor Green
