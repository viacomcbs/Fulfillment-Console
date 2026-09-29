# Split PROD runs: Orders LF (8 sessions) + Babloo table-view batches -> ONE email at end (~100 scenarios).
#
# Phase 1 - Regenerate LF passed session XMLs from tracker
# Phase 2 - Run 8 Orders LF sessions (no email); copy testng-results.xml each time
# Phase 3 - Run table-view split XML batches (no email); copy testng-results.xml each time
# Phase 4 - One consolidated email with all merged scenarios (passed-only by default for lead)
#
# Expected scenario count: ~57 LF passed + ~40 table-view = ~97-110 rows in email
#
# Examples:
#   .\scripts\run-lf-tableview-split-consolidated-email-prod.ps1
#   .\scripts\run-lf-tableview-split-consolidated-email-prod.ps1 -SkipTableView
#   .\scripts\run-lf-tableview-split-consolidated-email-prod.ps1 -ShowAllStatusesInEmail

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [switch] $SkipLeftFilters,
    [switch] $SkipTableView,
    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,

    # Lead email: passed scenarios only (default). Use -ShowAllStatusesInEmail for full pass/fail table.
    [switch] $ShowAllStatusesInEmail,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\consolidated-regression-aggregate"
$combinedSuite = "src/test/resources/regression/consolidated/LF_TV_ConsolidatedEmail_ProdServerSuite.xml"
$lfSessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"

# Babloo split table-view batches (~10 tests each, Synergy 30-min safe)
$tableViewBatches = @(
    "src/test/resources/regression/table-view/ColumnsSuite011-020Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite021-030Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite031-040Config.xml",
    "src/test/resources/regression/table-view/ColumnsSuite041-050Config.xml",
    "src/test/resources/regression/table-view/SortSuiteBatch1_127-136Config.xml"
)

function Invoke-MvnSuiteNoEmail {
    param(
        [string] $SuiteFile,
        [string] $Label
    )

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host $Label -ForegroundColor Cyan
    Write-Host "Suite: $SuiteFile" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    mvn -B test `
        "-DsuiteXmlFile=$SuiteFile" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    return $LASTEXITCODE
}

function Save-TestNgResults {
    param(
        [string] $OutputName
    )

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (-not (Test-Path $resultsFile)) {
        Write-Warning "Missing testng-results.xml for $OutputName"
        return $false
    }
    Copy-Item $resultsFile (Join-Path $aggregateDir "$OutputName-testng-results.xml") -Force
    return $true
}

Write-Host "=== LF + Table View split runs -> ONE consolidated email ===" -ForegroundColor Green
Write-Host "Email recipient : $Email"
Write-Host "Passed-only email: $(-not $ShowAllStatusesInEmail)"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

Write-Host "=== Regenerating LF passed session suite XMLs ==="
python scripts/build_lf_o_passed_session_suites.py

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

$failedRuns = New-Object System.Collections.Generic.List[string]
$runCount = 0

if (-not $SkipLeftFilters) {
    $sessionFiles = Get-ChildItem -Path (Join-Path $repoRoot $lfSessionsDir) -Filter "LF_O_Passed_S*.xml" | Sort-Object Name
    if ($sessionFiles.Count -eq 0) {
        throw "No LF_O_Passed_S*.xml session files found under $lfSessionsDir"
    }

    Write-Host ""
    Write-Host "=== ORDERS LEFT FILTERS ($($sessionFiles.Count) sessions) ===" -ForegroundColor Yellow
    $sessionNum = 0
    foreach ($sessionFile in $sessionFiles) {
        $sessionNum++
        $relSuite = $sessionFile.FullName.Substring($repoRoot.Length + 1) -replace "\\", "/"
        $code = Invoke-MvnSuiteNoEmail -SuiteFile $relSuite -Label "LF session $sessionNum/$($sessionFiles.Count): $($sessionFile.Name)"
        $runCount++
        if ($code -eq 0) {
            Save-TestNgResults -OutputName $sessionFile.BaseName | Out-Null
        } else {
            $failedRuns.Add("LF $($sessionFile.Name)") | Out-Null
            Save-TestNgResults -OutputName "$($sessionFile.BaseName)-partial" | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }
}

if (-not $SkipTableView -and (-not $StopOnFirstFailure -or $failedRuns.Count -eq 0)) {
    Write-Host ""
    Write-Host "=== TABLE VIEW ($($tableViewBatches.Count) split batches) ===" -ForegroundColor Yellow
    $batchNum = 0
    foreach ($batch in $tableViewBatches) {
        $batchNum++
        $batchName = [System.IO.Path]::GetFileNameWithoutExtension($batch)
        $code = Invoke-MvnSuiteNoEmail -SuiteFile $batch -Label "Table-view batch $batchNum/$($tableViewBatches.Count): $batchName"
        $runCount++
        if ($code -eq 0) {
            Save-TestNgResults -OutputName "TV_$batchName" | Out-Null
        } else {
            $failedRuns.Add("TV $batchName") | Out-Null
            Save-TestNgResults -OutputName "TV_${batchName}-partial" | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }
}

Write-Host ""
Write-Host "=== Sending ONE consolidated email ===" -ForegroundColor Yellow
$passedOnlyFlag = if ($ShowAllStatusesInEmail) { "false" } else { "true" }

if (-not $env:SLACK_WEBHOOK_URL) {
    Write-Warning "SLACK_WEBHOOK_URL is not set - Slack API post will be skipped (email-in Slack address still works)."
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
Write-Host "Done."
Write-Host "  Maven runs executed : $runCount"
Write-Host "  Aggregate dir       : test-output/consolidated-regression-aggregate"
Write-Host "  Email               : $Email (one consolidated message)"
Write-Host "  Passed-only table   : $(-not $ShowAllStatusesInEmail)"
if ($failedRuns.Count -gt 0) {
    Write-Host ""
    Write-Host "Runs with Maven failures (results still merged where available):" -ForegroundColor Yellow
    foreach ($name in $failedRuns) {
        Write-Host "  - $name" -ForegroundColor Yellow
    }
}
