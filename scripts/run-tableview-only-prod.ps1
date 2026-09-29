# Part 2 - Table view only (split Synergy batches) -> ONE email at end.
#
# Runs Babloo table-view batches (~40 scenarios), merges results, sends passed-only email
# to Akila + Slack.
#
# Examples:
#   .\scripts\run-tableview-only-prod.ps1
#   .\scripts\run-tableview-only-prod.ps1 -SkipCompile
#   .\scripts\run-tableview-only-prod.ps1 -FullRegression
#   .\scripts\run-tableview-only-prod.ps1 -ShowAllStatusesInEmail

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,
    [switch] $FullRegression,
    [switch] $ShowAllStatusesInEmail,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\tv-passed-aggregate"
$combinedSuite = "src/test/resources/regression/consolidated/TV_ConsolidatedEmail_ProdServerSuite.xml"

# Default: 5 split batches (~10 tests each, Synergy 30-min safe)
$splitBatches = @(
    "src/test/resources/regression/table-view/ColumnsSuite011-020Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite021-030Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite031-040Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite041-050Config.xml",
    "src/test/resources/regression/table-view/SortSuiteBatch1_127-136Config.xml"
)

$fullSuites = @(
    "src/test/resources/regression/table-view/ColumnsSuiteConfig.xml",
    "src/test/resources/regression/table-view/SortSuiteConfig.xml",
    "src/test/resources/regression/table-view/ColumnSearchSuiteConfig.xml",
    "src/test/resources/regression/table-view/ColumnSearchNegativeSuiteConfig.xml",
    "src/test/resources/regression/table-view/LineItemTabColumnsSuiteConfig.xml",
    "src/test/resources/regression/table-view/LineItemTabSuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD29174SuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD29174CountMatchSuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD29302SuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD29791SuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD30034SuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD30034SearchOnlySuiteConfig.xml",
    "src/test/resources/regression/table-view/BSD30034ExportSuiteConfig.xml"
)

$tableViewBatches = if ($FullRegression) { $fullSuites } else { $splitBatches }

Write-Host "=== Part 2: Table View only -> ONE email ===" -ForegroundColor Green
Write-Host "Email            : $Email"
Write-Host "Mode             : $(if ($FullRegression) { 'Full regression (13 suites)' } else { 'Split batches (5 suites, ~40 tests)' })"
Write-Host "Passed-only email: $(-not $ShowAllStatusesInEmail)"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

$failedRuns = New-Object System.Collections.Generic.List[string]
$batchNum = 0
foreach ($batch in $tableViewBatches) {
    $batchNum++
    $batchName = [System.IO.Path]::GetFileNameWithoutExtension($batch)

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Table-view batch $batchNum/$($tableViewBatches.Count): $batchName" -ForegroundColor Cyan
    Write-Host "Suite: $batch" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    mvn -B test `
        "-DsuiteXmlFile=$batch" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (Test-Path $resultsFile) {
        Copy-Item $resultsFile (Join-Path $aggregateDir "TV_${batchName}-testng-results.xml") -Force
    } else {
        Write-Warning "Missing testng-results.xml for $batchName"
    }

    if ($LASTEXITCODE -ne 0) {
        $failedRuns.Add($batchName) | Out-Null
        if ($StopOnFirstFailure) { break }
    }
}

Write-Host ""
Write-Host "=== Sending TABLE VIEW email ===" -ForegroundColor Yellow
$passedOnlyFlag = if ($ShowAllStatusesInEmail) { "false" } else { "true" }

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
    "-Dsystem.test.passedonlyconsolidatedemail=$passedOnlyFlag" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done - Part 2 (Table View)."
Write-Host "  Batches run   : $($tableViewBatches.Count)"
Write-Host "  Aggregate dir : test-output/tv-passed-aggregate"
Write-Host "  Email sent to : $Email"
if ($failedRuns.Count -gt 0) {
    Write-Host ""
    Write-Host "Batches with Maven failures (partial results still merged):" -ForegroundColor Yellow
    foreach ($name in $failedRuns) {
        Write-Host "  - $name" -ForegroundColor Yellow
    }
}
