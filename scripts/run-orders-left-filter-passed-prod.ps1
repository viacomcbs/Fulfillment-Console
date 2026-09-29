# Orders-view left filters — PROD Synergy, 8 sessions (30-min safe) + TWO emails + Slack.
#
# Stakeholder email (SendReportEmailAddress): passed scenarios only — no failures shown.
# Internal email (SendReportInternalEmailAddress): full pass/fail status — QA owner only.
#
# Example:
#   .\scripts\run-orders-left-filter-passed-prod.ps1
# Optional Slack webhook (in addition to Slack email-in recipient):
#   $env:SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/..."
#   .\scripts\run-orders-left-filter-passed-prod.ps1

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "=== Regenerating passed-only session suite XMLs from tracker ==="
python scripts/build_lf_o_passed_session_suites.py

Write-Host "=== Compiling tests ==="
mvn -B test-compile

$aggregateDir = Join-Path $repoRoot "test-output\lf-o-passed-aggregate"
$sessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"
$combinedSuite = "$sessionsDir/LF_O_Passed_CombinedEmail_ProdServerSuite.xml"

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

$sessionFiles = Get-ChildItem -Path (Join-Path $repoRoot $sessionsDir) -Filter "LF_O_Passed_S*.xml" | Sort-Object Name
if ($sessionFiles.Count -eq 0) {
    throw "No session suite XMLs found under $sessionsDir"
}

$sessionNum = 0
foreach ($sessionFile in $sessionFiles) {
    $sessionNum++
    Write-Host ""
    Write-Host "=== Session $sessionNum of $($sessionFiles.Count): $($sessionFile.Name) ==="
    $relSuite = $sessionFile.FullName.Substring($repoRoot.Length + 1) -replace "\\", "/"
    mvn -B test `
        "-DsuiteXmlFile=$relSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false"

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (-not (Test-Path $resultsFile)) {
        Write-Warning "Missing testng-results.xml after $($sessionFile.Name)"
        continue
    }
    Copy-Item $resultsFile (Join-Path $aggregateDir "$($sessionFile.BaseName)-testng-results.xml") -Force
}

Write-Host ""
Write-Host "=== Sending stakeholder email (passed only) + internal email (full status) + Slack ==="
if (-not $env:SLACK_WEBHOOK_URL) {
    Write-Warning @"
SLACK_WEBHOOK_URL is not set — #qa_automation_status_go will NOT get a Slack post.
Set it before this step (ask your lead for the incoming webhook URL):

  `$env:SLACK_WEBHOOK_URL = 'https://hooks.slack.com/services/...'
"@
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
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done."
Write-Host "  Sessions run       : $($sessionFiles.Count)"
Write-Host "  Aggregate dir      : test-output/lf-o-passed-aggregate"
Write-Host "  Stakeholder email  : $Email (passed scenarios only)"
Write-Host "  Internal email     : $InternalEmail (full pass/fail status)"
Write-Host "  Slack channel      : $SlackChannel (passed count only)"
