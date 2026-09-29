# Orders left filters - 3 filters, batch mode (1 Synergy session, refresh between filters, renewal at 20 min).
# Top 3 by latest PROD pass count (all 8/8):
#   1. Activity Type
#   2. Brand
#   3. Flag
#
# Examples:
#   .\scripts\run-lf-orders-3filters-sequential-prod.ps1
#   .\scripts\run-lf-orders-3filters-sequential-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-3filters-sequential-prod.ps1 -EmailOnly -SkipCompile

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

$batchSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_3filters_Batch_ProdServerSuite.xml"
$baseName = "LF_O_Split_3filters_Batch_ProdServerSuite"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-3filter-aggregate"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - 3 filters (batch continuous flow)"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"
$flowStepsDir = Join-Path $repoRoot "test-output\lf-o-flow-steps"

$selectedFilters = @(
    @{ key = "activitytype"; label = "Activity Type"; passes = "8/8" },
    @{ key = "brand";        label = "Brand";         passes = "8/8" },
    @{ key = "flag";         label = "Flag";          passes = "8/8" }
)

Write-Host "=== Orders Left Filters - 3 filters, batch mode ===" -ForegroundColor Green
Write-Host "Filters           : $($selectedFilters.Count)"
Write-Host "Mode              : 1 login, refresh between filters, Synergy renewal at 20 min" -ForegroundColor Yellow
Write-Host "Expected scenarios: $($selectedFilters.Count * 8) (8 per filter in email)" -ForegroundColor Yellow
Write-Host "Stakeholder email : $Email"
Write-Host "Internal email    : $InternalEmail"
Write-Host ""
Write-Host "Filter list:" -ForegroundColor Cyan
$rank = 0
foreach ($f in $selectedFilters) {
    $rank++
    Write-Host ("  {0}. {1,-18} latest PROD: {2}" -f $rank, $f.label, $f.passes)
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
            Write-Warning "Missing flow-steps XML (email may show fewer than 24 scenario rows)"
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
    $runLog = Join-Path $logDir "lf-3filter-batch_$stamp.log"

    Write-Host "=== Running 3-filter batch (single Maven / single Synergy session) ===" -ForegroundColor Cyan
    Write-Host "  Suite: $batchSuite"
    Write-Host "  Log  : $runLog" -ForegroundColor DarkGray
    Write-Host ""

    Add-Content -Path $runLog -Value "=== 3-filter batch started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ==="

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
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-3filter-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done - 3-filter batch continuous-flow run complete." -ForegroundColor Green
Write-Host "  Aggregate dir : test-output/lf-o-3filter-aggregate"
Write-Host "  Report title  : $reportTitle"
