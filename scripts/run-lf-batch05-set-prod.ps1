# Orders left filters - Batch of 5 filters in ONE Synergy session (calendar once).
# Set 1: Brand, Flag, Submitted By, Line Item Status, Activity Type
# Set 2: Order Status, Environment, Assigned To, Job type, Partner
#
# Examples:
#   .\scripts\run-lf-batch05-set-prod.ps1 -Set 1
#   .\scripts\run-lf-batch05-set-prod.ps1 -Set 2
#   .\scripts\run-lf-batch05-set-prod.ps1 -Set 1 -SkipCompile
#   .\scripts\run-lf-batch05-set-prod.ps1 -Set 1 -EmailOnly -SkipCompile

param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("1", "2")]
    [string] $Set,

    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $EmailOnly,
    [switch] $SkipEmail,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$setId = "Set0$Set"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-batch05-aggregate"
$sessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"
$batchSuite = "$sessionsDir/LF_O_Batch05_${setId}_ProdServerSuite.xml"
$combinedSuite = "$sessionsDir/LF_O_Batch05_${setId}_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD)"

$filterNames = switch ($Set) {
    "1" { "Brand, Flag, Submitted By, Line Item Status, Activity Type" }
    "2" { "Order Status, Environment, Assigned To, Job type, Partner" }
}

Write-Host "=== Orders Left Filters Batch05 Set $Set ===" -ForegroundColor Green
Write-Host "Filters - one Synergy login, calendar once: $filterNames"
Write-Host "Stakeholder email - passed only: $Email"
Write-Host "Internal email - full status: $InternalEmail"
if ($EmailOnly) {
    Write-Host "Mode - Email only, reuse test-output/lf-o-batch05-aggregate" -ForegroundColor Yellow
}
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

$mavenFailed = $false

if (-not $EmailOnly) {
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

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Running Batch05 $setId - 5 filters, 1 session" -ForegroundColor Cyan
    Write-Host "Suite: $batchSuite" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

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
        Copy-Item $resultsFile (Join-Path $aggregateDir "LF_O_Batch05_${setId}-testng-results.xml") -Force
        Write-Host "Saved results -> test-output/lf-o-batch05-aggregate/LF_O_Batch05_${setId}-testng-results.xml" -ForegroundColor Green
    } else {
        Write-Warning "Missing testng-results.xml after Batch05 $setId run"
    }

    if ($LASTEXITCODE -ne 0) {
        $mavenFailed = $true
        Write-Warning "Maven reported failures - partial results saved; consolidated email will still run."
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run full script first without -EmailOnly"
    }
    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    if ($xmlCount -lt 1) {
        throw "No result XML files in $aggregateDir"
    }
    Write-Host "Skipping Synergy - re-sending email from $xmlCount saved result file(s)" -ForegroundColor Yellow
}

if (-not $SkipEmail) {
    Write-Host ""
    Write-Host "=== Sending consolidated LEFT FILTER email - Batch05 Set $Set ===" -ForegroundColor Yellow
    Write-Host "  1) Stakeholder - passed scenarios only" -ForegroundColor Cyan
    Write-Host "  2) Internal - full pass/fail with Expected, Actual, Failure Reason" -ForegroundColor Cyan
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
        "-Dsystem.test.aggregateresultsdir=test-output/lf-o-batch05-aggregate" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=true" `
        "-Dsystem.test.slackchannel=$SlackChannel" `
        @slackArgs
}

Write-Host ""
Write-Host "Done."
Write-Host "  Batch set          : $setId - $filterNames"
Write-Host "  Aggregate dir      : test-output/lf-o-batch05-aggregate"
Write-Host "  Report title       : $reportTitle"
if ($mavenFailed) {
    Write-Host "  Note               : Some tests failed - check internal email for Expected, Actual, Failure Reason" -ForegroundColor Yellow
}
