# Orders left filters - Batch05 Set 1 (5 filters, one Synergy session).
# Filters: Brand, Flag, Submitted By, Line Item Status, Activity Type
#
# Usage:
#   .\scripts\run-lf-orders-5filters-set1-prod.ps1
#   .\scripts\run-lf-orders-5filters-set1-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-5filters-set1-prod.ps1 -SkipEmail
#   .\scripts\run-lf-orders-5filters-set1-prod.ps1 -SkipCompile -SkipEmail

param(
    [switch] $SkipCompile,
    [switch] $SkipEmail
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$sessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"
$batchSuite = "$sessionsDir/LF_O_Batch05_Set01_ProdServerSuite.xml"
$combinedSuite = "$sessionsDir/LF_O_Batch05_Set01_CombinedEmail_ProdServerSuite.xml"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-batch05-aggregate"
$reportTitle = "Orders Tab - Left Filters Regression (PROD)"

$email = (
    "Akilandeswari.Sundararajan@paramount.com," +
    "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
)
$internalEmail = "Akilandeswari.Sundararajan@paramount.com"

Write-Host "=== Orders Left Filters - 5 filters (Set 1) ===" -ForegroundColor Green
Write-Host "  1. Brand"
Write-Host "  2. Flag"
Write-Host "  3. Submitted By"
Write-Host "  4. Line Item Status"
Write-Host "  5. Activity Type"
Write-Host ""
Write-Host "Synergy live video: https://www.synergyserver.tech" -ForegroundColor Cyan
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ===" -ForegroundColor Cyan
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if (-not (Test-Path $batchSuite)) {
    Write-Host "Suite XML missing - generating Batch05 suites..." -ForegroundColor Yellow
    python scripts/build_lf_batch05_suites.py
    if ($LASTEXITCODE -ne 0) { throw "build_lf_batch05_suites.py failed" }
}

if (Test-Path $aggregateDir) {
    Get-ChildItem $aggregateDir -Filter "*.xml" -ErrorAction SilentlyContinue | Remove-Item -Force
} else {
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
}

Write-Host "=== Running 5 filters (1 Synergy session) ===" -ForegroundColor Cyan
Write-Host "Suite: $batchSuite"
Write-Host ""

$mavenFailed = $false
mvn -B test `
    "-DsuiteXmlFile=$batchSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=false" `
    "-Dsystem.test.leftfilteremailreport=true" `
    "-Dsystem.test.userkey=$akilaUserKey" `
    "-Dsystem.test.clientid=$akilaClientId"

$resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
if (Test-Path $resultsFile) {
    Copy-Item $resultsFile (Join-Path $aggregateDir "LF_O_Batch05_Set01-testng-results.xml") -Force
    Write-Host "Saved results -> test-output/lf-o-batch05-aggregate/LF_O_Batch05_Set01-testng-results.xml" -ForegroundColor Green
}

if ($LASTEXITCODE -ne 0) {
    $mavenFailed = $true
    Write-Warning "Some tests failed - partial results saved."
}

if (-not $SkipEmail) {
    Write-Host ""
    Write-Host "=== Sending consolidated email ===" -ForegroundColor Yellow
    mvn -B test `
        "-DsuiteXmlFile=$combinedSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportemailaddress=$email" `
        "-Dsystem.test.sendreportinternalemailaddress=$internalEmail" `
        "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
        "-Dsystem.test.aggregateresultsdir=test-output/lf-o-batch05-aggregate" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=true" `
        "-Dsystem.test.slackchannel=#qa_automation_status_go"
}

Write-Host ""
Write-Host "Done." -ForegroundColor Green
if ($mavenFailed) {
    Write-Host "Note: Some tests failed - check console output or internal email." -ForegroundColor Yellow
}
