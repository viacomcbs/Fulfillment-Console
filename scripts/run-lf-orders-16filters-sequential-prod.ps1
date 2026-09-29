# Orders left filters - SINGLE thread, 16 filters, batch mode (1 login per ~20 min, refresh between filters).
# Copies flow-steps + testng-results for consolidated email (8 scenario rows per filter).
#
# Examples:
#   .\scripts\run-lf-orders-16filters-sequential-prod.ps1
#   .\scripts\run-lf-orders-16filters-sequential-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-16filters-sequential-prod.ps1 -EmailOnly -SkipCompile

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $EmailOnly,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$batchSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_16filters_Batch_ProdServerSuite.xml"
$baseName = "LF_O_Split_16filters_Batch_ProdServerSuite"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-16filter-aggregate"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - 16 filters (batch continuous flow)"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"
$flowStepsDir = Join-Path $repoRoot "test-output\lf-o-flow-steps"

$selectedFilters = @(
    @{ key = "activitytype";     label = "Activity Type";      passes = "8/8" },
    @{ key = "brand";            label = "Brand";              passes = "8/8" },
    @{ key = "flag";             label = "Flag";               passes = "8/8" },
    @{ key = "orderstatus";      label = "Order Status";       passes = "8/8" },
    @{ key = "submittedby";      label = "Submitted by";       passes = "8/8" },
    @{ key = "job";              label = "Job type";           passes = "8/9" },
    @{ key = "errormessage";     label = "Error message";      passes = "8/8" },
    @{ key = "deliveryprotocol"; label = "Delivery Protocol";  passes = "8/8" },
    @{ key = "demandsystem";     label = "Demand system";      passes = "8/8" },
    @{ key = "language";         label = "Language";           passes = "8/8" },
    @{ key = "region";           label = "Region";             passes = "8/8" },
    @{ key = "assignedto";       label = "Assigned To";        passes = "7/8" },
    @{ key = "lineitemstatus";   label = "Line Item Status";   passes = "7/8" },
    @{ key = "environment";      label = "Environment";        passes = "7/8" },
    @{ key = "contenttype";      label = "Content type";       passes = "7/8" },
    @{ key = "partner";           label = "Partner";            passes = "3/8" }
)

Write-Host "=== Orders Left Filters - 16 filters, batch mode ===" -ForegroundColor Green
Write-Host "Filters           : $($selectedFilters.Count)"
Write-Host "Mode              : 1 login per ~20 min, refresh between filters, auto Synergy renewal" -ForegroundColor Yellow
Write-Host "Expected scenarios: $($selectedFilters.Count * 8) (8 per filter in email)" -ForegroundColor Yellow
Write-Host "Stakeholder email : $Email"
Write-Host "Internal email    : $InternalEmail"
if ($EmailOnly) {
    Write-Host "Run mode          : Email only (reuse test-output/lf-o-16filter-aggregate)" -ForegroundColor Yellow
}
Write-Host ""

if (-not $SkipCompile -and -not $EmailOnly) {
    Write-Host "=== Compiling tests ===" -ForegroundColor Cyan
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

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

function Copy-LfBatchAggregateResults {
    param(
        [string] $BaseName,
        [string] $DestDir
    )

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (Test-Path $resultsFile) {
        Copy-Item $resultsFile (Join-Path $DestDir "${BaseName}-testng-results.xml") -Force
        Write-Host "  Saved: ${BaseName}-testng-results.xml" -ForegroundColor DarkGray
    } else {
        Write-Warning "Missing testng-results.xml after batch run"
    }

    if (Test-Path $flowStepsDir) {
        $flowFiles = @(Get-ChildItem $flowStepsDir -Filter "*-flow-steps.xml" -ErrorAction SilentlyContinue)
        if ($flowFiles.Count -gt 0) {
            $latestFlow = $flowFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1
            Copy-Item $latestFlow.FullName (Join-Path $DestDir "${BaseName}-flow-steps.xml") -Force
            Write-Host "  Saved: ${BaseName}-flow-steps.xml ($($latestFlow.Name))" -ForegroundColor DarkGray
        } else {
            Write-Warning "Missing flow-steps XML (email may show fewer scenario rows)"
        }
    }
}

$runLog = $null
$batchExitCode = 0

if (-not $EmailOnly) {
    Stop-StaleLfMavenProcesses

    Write-Host "=== Pre-flight: Synergy server check ===" -ForegroundColor Cyan
    if (-not (Test-SynergyServerReachable)) {
        throw "Synergy server is not reachable. Check network/VPN, then re-run."
    }
    Write-Host "Note: Synergy renews automatically at ~20 min (public lab 30 min max per session)." -ForegroundColor DarkGray
    Write-Host ""

    if (-not (Test-Path (Join-Path $repoRoot ($batchSuite -replace '/', '\')))) {
        throw "Batch suite missing: $batchSuite"
    }

    if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    if (Test-Path $flowStepsDir) { Remove-Item -Recurse -Force $flowStepsDir }
    New-Item -ItemType Directory -Force -Path $flowStepsDir | Out-Null

    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $runLog = Join-Path $logDir "lf-16filter-batch_$stamp.log"

    Write-Host "=== Running 16-filter batch (single Maven, shared session with renewals) ===" -ForegroundColor Cyan
    Write-Host "  Suite: $batchSuite"
    Write-Host "  Log  : $runLog" -ForegroundColor DarkGray
    Write-Host ""

    Add-Content -Path $runLog -Value "=== 16-filter batch started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ==="

    mvn -B test `
        "-DsuiteXmlFile=$batchSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    $batchExitCode = $LASTEXITCODE
    Copy-LfBatchAggregateResults -BaseName $baseName -DestDir $aggregateDir

    Write-Host ""
    Write-Host "Full run log: $runLog" -ForegroundColor DarkGray
    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    Write-Host "Aggregate XML files: $xmlCount" -ForegroundColor $(if ($xmlCount -ge 1) { "Green" } else { "Yellow" })

    if ($batchExitCode -ne 0) {
        Write-Host "Batch run finished with failures (exit $batchExitCode)" -ForegroundColor Yellow
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run without -EmailOnly first"
    }
}

Write-Host ""
Write-Host "=== Sending consolidated LEFT FILTER emails ===" -ForegroundColor Yellow

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
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-16filter-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done - Orders left-filter 16-filter batch run." -ForegroundColor Green
Write-Host "  Aggregate dir : test-output/lf-o-16filter-aggregate"
Write-Host "  Report title  : $reportTitle"
