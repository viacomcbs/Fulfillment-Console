# Orders left filters - Franchise only on PROD.
#
# Default: re-run FAILED scenarios only (4 tests) and copy results into daily-aggregate
# so send-daily-consolidated-email.ps1 upgrades FAIL -> PASS (dedupe prefers pass).
#
# Examples:
#   .\scripts\run-lf-orders-franchise-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-franchise-prod.ps1 -SkipCompile -ResendConsolidatedEmail
#   .\scripts\run-lf-orders-franchise-prod.ps1 -AllScenarios -SkipCompile
#   .\scripts\run-lf-orders-franchise-prod.ps1 -SkipCompile -SkipEmail -NoUpdateDailyAggregate

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $AllScenarios,
    [switch] $SkipCompile,
    [switch] $SkipEmail,
    [switch] $EmailOnly,

    [switch] $NoUpdateDailyAggregate,
    [switch] $ResendConsolidatedEmail,

    [string] $Date = (Get-Date -Format "yyyy-MM-dd"),

    [string] $ConsolidatedTitle = "Orders Left Filters (PROD) - All batches today",

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$filterLabel = "Franchise"
$failedOnlySuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Franchise_FailedOnly_ProdServerSuite.xml"
$allScenariosSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_franchise_ProdServerSuite.xml"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-franchise-aggregate"
$dailyAggregateDir = Join-Path $repoRoot ("test-output\daily-aggregate\{0}" -f $Date)
$logDir = Join-Path $repoRoot "test-output\parallel-logs"
$flowStepsDir = Join-Path $repoRoot "test-output\lf-o-flow-steps"

$useFailedOnly = -not $AllScenarios
if ($useFailedOnly) {
    $suiteRel = $failedOnlySuite
    $baseName = "LF_O_Franchise_FailedOnly_ProdServerSuite"
    $reportTitle = "Orders Tab - Left Filters (PROD) - Franchise (failed-only rerun)"
    $modeLabel = "4 failed scenarios only (Search, Table Sync, Active Filters, Clear Filters)"
} else {
    $suiteRel = $allScenariosSuite
    $baseName = "LF_O_Split_franchise_ProdServerSuite"
    $reportTitle = "Orders Tab - Left Filters (PROD) - Franchise (continuous flow)"
    $modeLabel = "All scenarios - 1 Synergy login, continuous flow"
}

Write-Host "=== Orders Left Filters - Franchise ===" -ForegroundColor Green
Write-Host "Filter            : $filterLabel"
Write-Host "Mode              : $modeLabel" -ForegroundColor Yellow
Write-Host "Daily aggregate   : $(if ($NoUpdateDailyAggregate) { '(skipped)' } else { $dailyAggregateDir })"
Write-Host "Stakeholder email : $(if ($SkipEmail) { '(skipped)' } else { $Email })"
Write-Host "Internal email    : $(if ($SkipEmail) { '(skipped)' } else { $InternalEmail })"
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
        Where-Object { $_.CommandLine -and $_.CommandLine -match 'LF_O_(Split_franchise|Franchise_FailedOnly).*ProdServerSuite' }
    if ($stale) {
        Write-Host "Stopping stale Franchise Maven/Synergy process(es): $($stale.Count)" -ForegroundColor Yellow
        foreach ($p in $stale) {
            Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 3
    }
}

function Copy-LfAggregateResults {
    param(
        [string] $BaseName,
        [string] $DestDir
    )

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (Test-Path $resultsFile) {
        $destPath = Join-Path $DestDir "${BaseName}-testng-results.xml"
        Copy-Item $resultsFile $destPath -Force
        Write-Host "  Saved: ${BaseName}-testng-results.xml" -ForegroundColor DarkGray
        return $destPath
    }

    Write-Warning "Missing testng-results.xml"
    return $null
}

function Update-DailyAggregateFromRerun {
    param(
        [string] $SourceResultsFile,
        [string] $DestDir
    )

    if (-not $SourceResultsFile -or -not (Test-Path $SourceResultsFile)) {
        Write-Warning "No rerun results to copy into daily aggregate."
        return
    }

    if (-not (Test-Path $DestDir)) {
        New-Item -ItemType Directory -Force -Path $DestDir | Out-Null
        Write-Host "Created daily aggregate folder: $DestDir" -ForegroundColor Yellow
    }

    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $suffix = if ($useFailedOnly) { "franchise-failed-only-rerun" } else { "franchise-all-scenarios-rerun" }
    $destName = "{0}-{1}-testng-results.xml" -f $suffix, $stamp
    $destPath = Join-Path $DestDir $destName
    Copy-Item $SourceResultsFile $destPath -Force
    Write-Host "Daily aggregate updated: $destPath" -ForegroundColor Green
    Write-Host "  Consolidated email will dedupe by test class and prefer PASS over FAIL." -ForegroundColor DarkGray
}

$runLog = $null
$testExit = 0
$latestResults = $null

if (-not $EmailOnly) {
    Stop-StaleLfMavenProcesses

    Write-Host "=== Pre-flight: Synergy server check ===" -ForegroundColor Cyan
    if (-not (Test-SynergyServerReachable)) {
        throw "Synergy server is not reachable. Check network/VPN, then re-run."
    }
    Write-Host ""

    if (-not (Test-Path (Join-Path $repoRoot ($suiteRel -replace '/', '\')))) {
        throw "Suite file missing: $suiteRel"
    }

    if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null

    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $runLog = Join-Path $logDir ("lf-franchise_{0}.log" -f $stamp)

    Write-Host "=== Running $filterLabel ===" -ForegroundColor Cyan
    Write-Host "  Suite: $suiteRel"
    Write-Host "  Log  : $runLog" -ForegroundColor DarkGray
    Write-Host ""

    Add-Content -Path $runLog -Value "=== $filterLabel started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') mode=$modeLabel ==="

    if ($AllScenarios -and (Test-Path $flowStepsDir)) {
        Remove-Item -Recurse -Force $flowStepsDir
        New-Item -ItemType Directory -Force -Path $flowStepsDir | Out-Null
    }

    mvn -B test `
        "-DsuiteXmlFile=$suiteRel" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    $testExit = $LASTEXITCODE
    $latestResults = Copy-LfAggregateResults -BaseName $baseName -DestDir $aggregateDir

    if (-not $NoUpdateDailyAggregate) {
        Update-DailyAggregateFromRerun -SourceResultsFile $latestResults -DestDir $dailyAggregateDir
    }

    if ($testExit -ne 0) {
        Write-Host "Franchise run finished with failures (exit $testExit)" -ForegroundColor Yellow
    }

    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    Write-Host "Aggregate XML files: $xmlCount" -ForegroundColor $(if ($xmlCount -ge 1) { "Green" } else { "Yellow" })
    Write-Host "Full run log: $runLog" -ForegroundColor DarkGray
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run without -EmailOnly first"
    }
}

if (-not $SkipEmail) {
    Write-Host ""
    Write-Host "=== Sending Franchise LEFT FILTER emails ===" -ForegroundColor Yellow

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
        "-Dsystem.test.aggregateresultsdir=test-output/lf-o-franchise-aggregate" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=true" `
        "-Dsystem.test.slackchannel=$SlackChannel" `
        @slackArgs

    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Franchise email step failed (exit $LASTEXITCODE)"
    }
}

if ($ResendConsolidatedEmail) {
    Write-Host ""
    Write-Host "=== Re-sending daily consolidated email ===" -ForegroundColor Yellow
    & (Join-Path $repoRoot "scripts\send-daily-consolidated-email.ps1") `
        -Date $Date `
        -Title $ConsolidatedTitle
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Daily consolidated email failed (exit $LASTEXITCODE)"
    }
}

Write-Host ""
Write-Host "Done - Franchise run complete." -ForegroundColor Green
Write-Host "  Aggregate dir     : test-output/lf-o-franchise-aggregate"
if (-not $NoUpdateDailyAggregate) {
    Write-Host "  Daily aggregate   : test-output/daily-aggregate/$Date"
}
if (-not $ResendConsolidatedEmail) {
    Write-Host ""
    Write-Host "To refresh consolidated email after rerun:" -ForegroundColor Cyan
    Write-Host "  .\scripts\send-daily-consolidated-email.ps1 -Date $Date"
}

if ($testExit -ne 0) { exit $testExit }
