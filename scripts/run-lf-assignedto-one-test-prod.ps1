# Assigned To — ONE test (TC109 Basic) on PROD -> ONE stakeholder email (passed-only).
#
# Use this to verify email format and recipients before run-lf-only-prod.ps1 (~52 scenarios).
#
# Examples:
#   .\scripts\run-lf-assignedto-one-test-prod.ps1
#   .\scripts\run-lf-assignedto-one-test-prod.ps1 -SkipEmail
#   .\scripts\run-lf-assignedto-one-test-prod.ps1 -Email "Akilandeswari.Sundararajan@paramount.com"

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

$singleTestSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Passed_S02_Assignedto_TC109_Smoke_ProdServerSuite.xml"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Passed_CombinedEmail_ProdServerSuite.xml"
$aggregateDir = Join-Path $repoRoot "test-output\lf-assignedto-one-test-aggregate"
$reportTitle = "Assigned To TC109 Smoke - Left Filters (PROD)"
$testCaseId = "LF_O_TC109"

Write-Host "=== Assigned To ONE test smoke (TC109 Basic) ===" -ForegroundColor Green
Write-Host "Suite       : $singleTestSuite"
Write-Host "Test case   : $testCaseId"
Write-Host "Stakeholder : $(if ($SkipEmail) { '(skipped)' } else { $Email })"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

Write-Host ""
Write-Host "=== Running TC109 on PROD Synergy (no email yet) ===" -ForegroundColor Cyan
mvn -B test `
    "-DsuiteXmlFile=$singleTestSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=false" `
    "-Dsystem.test.userkey=$akilaUserKey" `
    "-Dsystem.test.clientid=$akilaClientId"

$testExit = $LASTEXITCODE
$resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
if (-not (Test-Path $resultsFile)) {
    throw "Missing testng-results.xml after TC109 run"
}
Copy-Item $resultsFile (Join-Path $aggregateDir "LF_O_Passed_S02_Assignedto_TC109-testng-results.xml") -Force

if ($SkipEmail) {
    Write-Host ""
    Write-Host "Done (email skipped). Results: test-output/lf-assignedto-one-test-aggregate"
    exit $testExit
}

Write-Host ""
Write-Host "=== Sending ONE stakeholder email (passed-only) ===" -ForegroundColor Yellow
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
    '-Dsystem.test.sendreportinternalemailaddress=' `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=test-output/lf-assignedto-one-test-aggregate" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

$emailExit = $LASTEXITCODE

Write-Host ""
Write-Host "Done."
if ($testExit -ne 0) {
    Write-Host "  TC109 run exit code : $testExit (check Synergy if non-zero)" -ForegroundColor Yellow
}
if ($emailExit -ne 0) {
    Write-Host "  Email step exit code: $emailExit" -ForegroundColor Yellow
}
Write-Host "  Expected test case ID in email : $testCaseId"
Write-Host "  Report title                   : $reportTitle"
Write-Host "  Stakeholder email sent to      : $Email"
Write-Host ""
Write-Host "If email looks good, run full LF passed suite (~52 scenarios):"
Write-Host "  .\scripts\run-lf-only-prod.ps1"

exit [Math]::Max($testExit, $emailExit)
