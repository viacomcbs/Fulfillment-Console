# Preview Assigned To left-filter tests only (7 scenarios) -> ONE email to check report format.
#
# Examples:
#   .\scripts\run-lf-assignedto-preview-prod.ps1
#   .\scripts\run-lf-assignedto-preview-prod.ps1 -Email "Akilandeswari.Sundararajan@paramount.com"
#   .\scripts\run-lf-assignedto-preview-prod.ps1 -SkipEmail

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [switch] $SkipCompile,
    [switch] $SkipEmail,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$assignedToSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Passed_S02_Assignedto_ProdServerSuite.xml"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Passed_CombinedEmail_ProdServerSuite.xml"
$aggregateDir = Join-Path $repoRoot "test-output\lf-assignedto-preview-aggregate"
$reportTitle = "Assigned To - Left Filters Report Preview (PROD)"

Write-Host "=== Assigned To preview (7 tests) ===" -ForegroundColor Green
Write-Host "Suite  : $assignedToSuite"
Write-Host "Email  : $(if ($SkipEmail) { '(skipped)' } else { $Email })"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

Write-Host ""
Write-Host "=== Running Assigned To tests (no email yet) ===" -ForegroundColor Cyan
mvn -B test `
    "-DsuiteXmlFile=$assignedToSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=false" `
    "-Dsystem.test.userkey=$akilaUserKey" `
    "-Dsystem.test.clientid=$akilaClientId"

$resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
if (-not (Test-Path $resultsFile)) {
    throw "Missing testng-results.xml after Assigned To run"
}
Copy-Item $resultsFile (Join-Path $aggregateDir "LF_O_Passed_S02_Assignedto-testng-results.xml") -Force

if ($SkipEmail) {
    Write-Host ""
    Write-Host "Done (email skipped). Results: test-output/lf-assignedto-preview-aggregate"
    exit 0
}

Write-Host ""
Write-Host "=== Sending preview email (passed-only report format) ===" -ForegroundColor Yellow
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
    "-Dsystem.test.sendreportinternalemailaddress=" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=test-output/lf-assignedto-preview-aggregate" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done."
if ($LASTEXITCODE -ne 0 -and (Test-Path $resultsFile)) {
    Write-Host "  Note: Maven exit code was non-zero (Synergy disconnect is common). Partial results were merged into the email." -ForegroundColor Yellow
}
Write-Host "  Tests attempted : 7 (Assigned To filter)"
Write-Host "  Report title  : $reportTitle"
Write-Host "  Email sent to : $Email"
Write-Host ""
Write-Host "If format looks good, run full LF part:"
Write-Host "  .\scripts\run-lf-only-prod.ps1"
