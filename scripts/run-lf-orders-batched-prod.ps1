# Orders left filters - batched PROD runs (continuous flow: 1 test per filter, no browser refresh).
#
# 21 unique Orders-view filters in 4 batches (Job type counted once; legacy jobtype folder excluded):
#   Batch 1 (3):  Activity Type, Brand, Flag
#   Batch 2 (5):  Order Status, Submitted by, Error message, Delivery Protocol, Demand system
#   Batch 3 (7):  Language, Region, Assigned To, Line Item Status, Environment, Content type, Franchise
#   Batch 4 (7):  Job type, Partner, Series title, Season number, Episode number, System Name, (reserved*)
#
# *Batch 4 has 6 unique filters remaining after batches 1-3 (21 total). Run -SendCombinedEmail after batch 4.
#
# Shared aggregate across batches — do NOT delete aggregate when running batch 2/3/4.
#
# Examples:
#   .\scripts\run-lf-orders-batched-prod.ps1 -Batch 2 -SkipCompile
#   .\scripts\run-lf-orders-batched-prod.ps1 -Batch 3 -SkipCompile
#   .\scripts\run-lf-orders-batched-prod.ps1 -Batch 4 -SkipCompile -SendCombinedEmail
#   .\scripts\run-lf-orders-batched-prod.ps1 -Batch All -SkipCompile -SendCombinedEmail

param(
    [ValidateSet(1, 2, 3, 4, "All")]
    [string] $Batch = "All",

    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,
    [switch] $SendCombinedEmail,
    [switch] $EmailOnly,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\lf-o-full-batch-aggregate"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - All filters (continuous flow)"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"

$allBatches = @{
    1 = @(
        @{ key = "activitytype";     label = "Activity Type";     passes = "8/8" },
        @{ key = "brand";            label = "Brand";             passes = "8/8" },
        @{ key = "flag";             label = "Flag";              passes = "8/8" }
    )
    2 = @(
        @{ key = "orderstatus";      label = "Order Status";      passes = "8/8" },
        @{ key = "submittedby";      label = "Submitted by";      passes = "8/8" },
        @{ key = "errormessage";     label = "Error message";     passes = "8/8" },
        @{ key = "deliveryprotocol"; label = "Delivery Protocol"; passes = "8/8" },
        @{ key = "demandsystem";     label = "Demand system";     passes = "8/8" }
    )
    3 = @(
        @{ key = "language";         label = "Language";          passes = "8/8" },
        @{ key = "region";           label = "Region";            passes = "8/8" },
        @{ key = "assignedto";       label = "Assigned To";       passes = "7/8" },
        @{ key = "lineitemstatus";   label = "Line Item Status";  passes = "7/8" },
        @{ key = "environment";      label = "Environment";       passes = "7/8" },
        @{ key = "contenttype";      label = "Content type";      passes = "7/8" },
        @{ key = "franchise";        label = "Franchise";         passes = "8/8" }
    )
    4 = @(
        @{ key = "job";              label = "Job type";          passes = "8/9" },
        @{ key = "partner";          label = "Partner";           passes = "3/8" },
        @{ key = "seriestitle";      label = "Series title";      passes = "8/8" },
        @{ key = "seasonnumber";      label = "Season number";     passes = "8/8" },
        @{ key = "episodenumber";    label = "Episode number";    passes = "8/8" },
        @{ key = "systemname";       label = "System Name";       passes = "8/8" }
    )
}

$totalFilters = ($allBatches.Values | ForEach-Object { $_.Count } | Measure-Object -Sum).Sum

function Test-SynergyServerReachable {
    $url = "https://www.synergyserver.tech"
    try {
        Invoke-WebRequest -Uri $url -TimeoutSec 15 -UseBasicParsing | Out-Null
        Write-Host "Synergy server reachable: $url" -ForegroundColor Green
        return $true
    } catch {
        Write-Warning "Synergy server not reachable: $url"
        return $false
    }
}

function Stop-StaleLfMavenProcesses {
    $stale = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -and $_.CommandLine -match 'LF_O_Split_.*_ProdServerSuite' }
    if ($stale) {
        Write-Host "Stopping stale LF Maven/Synergy test process(es): $($stale.Count)" -ForegroundColor Yellow
        foreach ($p in $stale) {
            Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 3
    }
}

function Run-FilterBatch {
    param(
        [int] $BatchNumber,
        [array] $Filters,
        [bool] $ClearAggregate
    )

    if ($ClearAggregate -and (Test-Path $aggregateDir)) {
        Remove-Item -Recurse -Force $aggregateDir
    }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null

    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $runLog = Join-Path $logDir "lf-batch${BatchNumber}_$stamp.log"

    Write-Host ""
    Write-Host "========== BATCH $BatchNumber ($($Filters.Count) filters) ==========" -ForegroundColor Magenta
    Write-Host "Aggregate dir: $aggregateDir (append mode)" -ForegroundColor DarkGray
    Write-Host "Log file     : $runLog" -ForegroundColor DarkGray
    Write-Host ""

    $failedRuns = New-Object System.Collections.Generic.List[string]
    $num = 0

    foreach ($filter in $Filters) {
        $num++
        $key = $filter.key
        $label = $filter.label
        $suiteRel = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_${key}_ProdServerSuite.xml"
        $baseName = "LF_O_Split_${key}_ProdServerSuite"

        if (-not (Test-Path (Join-Path $repoRoot ($suiteRel -replace '/', '\')))) {
            throw "Suite file missing for $label : $suiteRel"
        }

        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "[Batch $BatchNumber | Filter $num/$($Filters.Count)] $label (latest PROD: $($filter.passes))" -ForegroundColor Cyan
        Write-Host "  Suite: $suiteRel"
        Write-Host "========================================" -ForegroundColor Cyan

        Add-Content -Path $runLog -Value "=== Batch $BatchNumber [$num/$($Filters.Count)] $label started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ==="

        mvn -B test `
            "-DsuiteXmlFile=$suiteRel" `
            "-Dsystem.test.testenvironment=PROD" `
            "-Dsystem.test.sendreportautoemails=false" `
            "-Dsystem.test.sendchatreport=false" `
            "-Dsystem.test.userkey=$akilaUserKey" `
            "-Dsystem.test.clientid=$akilaClientId"

        $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
        $destName = "${baseName}-testng-results.xml"
        $destPath = Join-Path $aggregateDir $destName

        if (Test-Path $resultsFile) {
            Copy-Item $resultsFile $destPath -Force
            Write-Host "  Saved: $destName" -ForegroundColor DarkGray
        } else {
            Write-Warning "Missing testng-results.xml after $label"
        }

        if ($LASTEXITCODE -ne 0) {
            $failedRuns.Add("$label|$suiteRel") | Out-Null
            if ($StopOnFirstFailure) {
                throw "Stopped on first failure: $label"
            }
        }
    }

    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml" -ErrorAction SilentlyContinue).Count
    Write-Host ""
    Write-Host "Batch $BatchNumber complete. Aggregate XML files so far: $xmlCount" -ForegroundColor Green
    if ($failedRuns.Count -gt 0) {
        Write-Host "Batch $BatchNumber failures: $($failedRuns.Count)" -ForegroundColor Yellow
        foreach ($f in $failedRuns) { Write-Host "  - $f" -ForegroundColor Yellow }
    }

    return $failedRuns
}

function Send-CombinedLeftFilterEmail {
    Write-Host ""
    Write-Host "=== Sending ONE combined LEFT FILTER email ($totalFilters filters) ===" -ForegroundColor Yellow
    Write-Host "  Aggregate dir: test-output/lf-o-full-batch-aggregate" -ForegroundColor Cyan

    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml" -ErrorAction SilentlyContinue).Count
    if ($xmlCount -lt 1) {
        throw "No result XML files in $aggregateDir — run batches first"
    }
    Write-Host "  XML files in aggregate: $xmlCount" -ForegroundColor Cyan

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
        "-Dsystem.test.aggregateresultsdir=test-output/lf-o-full-batch-aggregate" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=true" `
        "-Dsystem.test.slackchannel=$SlackChannel" `
        @slackArgs
}

Write-Host "=== Orders Left Filters - Batched PROD Run ===" -ForegroundColor Green
Write-Host "Total unique filters : $totalFilters (Job type once; legacy jobtype excluded)"
Write-Host "Batch plan           : 3 + 5 + 7 + 6 = $totalFilters"
Write-Host "Aggregate dir        : test-output/lf-o-full-batch-aggregate"
Write-Host ""

if (-not $SkipCompile -and -not $EmailOnly) {
    Write-Host "=== Compiling tests ===" -ForegroundColor Cyan
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if ($EmailOnly) {
    Send-CombinedLeftFilterEmail
    Write-Host "Done - combined email sent." -ForegroundColor Green
    exit 0
}

Stop-StaleLfMavenProcesses

Write-Host "=== Pre-flight: Synergy server check ===" -ForegroundColor Cyan
if (-not (Test-SynergyServerReachable)) {
    throw "Synergy server is not reachable. Check network/VPN, then re-run."
}

$batchesToRun = @()
if ($Batch -eq "All") {
    $batchesToRun = @(1, 2, 3, 4)
} else {
    $batchesToRun = @([int]$Batch)
}

$allFailed = New-Object System.Collections.Generic.List[string]

foreach ($b in $batchesToRun) {
    $filters = $allBatches[$b]
    Write-Host ""
    Write-Host "Batch $b filters:" -ForegroundColor Cyan
    foreach ($f in $filters) { Write-Host "  - $($f.label)" }

    # Clear aggregate only when batch 1 starts a full run; batches 2-4 always append.
    $clear = ($b -eq 1) -and (-not (Test-Path $aggregateDir) -or $Batch -eq "All")

    $failed = Run-FilterBatch -BatchNumber $b -Filters $filters -ClearAggregate $clear
    foreach ($f in $failed) { $allFailed.Add($f) | Out-Null }
}

if ($SendCombinedEmail -or ($Batch -eq "All")) {
    Send-CombinedLeftFilterEmail
}

Write-Host ""
Write-Host "Done." -ForegroundColor Green
Write-Host "  Batches run    : $($batchesToRun -join ', ')"
Write-Host "  Aggregate dir  : test-output/lf-o-full-batch-aggregate"
if ($allFailed.Count -gt 0) {
    Write-Host "  Total failures : $($allFailed.Count)" -ForegroundColor Yellow
} else {
    Write-Host "  All batch filters passed Maven exit code." -ForegroundColor Green
}
