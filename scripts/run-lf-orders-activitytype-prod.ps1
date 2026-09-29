# Orders left filters - Activity Type only, continuous-flow (8 scenarios in one browser session).
#
# Examples:
#   .\scripts\run-lf-orders-activitytype-prod.ps1
#   .\scripts\run-lf-orders-activitytype-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-activitytype-prod.ps1 -EmailOnly -SkipCompile

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

$filterKey = "activitytype"
$filterLabel = "Activity Type"
$suiteRel = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_${filterKey}_ProdServerSuite.xml"
$baseName = "LF_O_Split_${filterKey}_ProdServerSuite"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-activitytype-aggregate"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - Activity Type (continuous flow)"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"
$flowStepsDir = Join-Path $repoRoot "test-output\lf-o-flow-steps"

Write-Host "=== Orders Left Filters - Activity Type only (continuous flow) ===" -ForegroundColor Green
Write-Host "Filter            : $filterLabel"
Write-Host "Mode              : 1 Synergy login, 8 steps in one test (no refresh)" -ForegroundColor Yellow
Write-Host "Stakeholder email : $Email"
Write-Host "Internal email    : $InternalEmail"
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
    } else {
        Write-Warning "Missing testng-results.xml"
    }

    $flowFiles = @()
    if (Test-Path $flowStepsDir) {
        $flowFiles = @(Get-ChildItem $flowStepsDir -Filter "*-flow-steps.xml" -ErrorAction SilentlyContinue)
    }
    if ($flowFiles.Count -gt 0) {
        $latestFlow = $flowFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        $flowDest = Join-Path $DestDir "${BaseName}-flow-steps.xml"
        Copy-Item $latestFlow.FullName $flowDest -Force
        Write-Host "  Saved: ${BaseName}-flow-steps.xml ($($latestFlow.Name))" -ForegroundColor DarkGray
    } else {
        Write-Warning "Missing flow-steps XML (email may show 1 row instead of 8)"
    }
}

$runLog = $null

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
    $runLog = Join-Path $logDir "lf-activitytype_$stamp.log"

    Write-Host "=== Running $filterLabel continuous flow ===" -ForegroundColor Cyan
    Write-Host "  Suite: $suiteRel"
    Write-Host "  Log  : $runLog" -ForegroundColor DarkGray
    Write-Host ""

    Add-Content -Path $runLog -Value "=== $filterLabel started $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ==="

    if (Test-Path $flowStepsDir) { Remove-Item -Recurse -Force $flowStepsDir }
    New-Item -ItemType Directory -Force -Path $flowStepsDir | Out-Null

    mvn -B test `
        "-DsuiteXmlFile=$suiteRel" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    Copy-LfAggregateResults -BaseName $baseName -DestDir $aggregateDir

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Activity Type flow finished with failures (exit $LASTEXITCODE)" -ForegroundColor Yellow
    }

    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    Write-Host "Aggregate XML files: $xmlCount" -ForegroundColor $(if ($xmlCount -ge 1) { "Green" } else { "Yellow" })
    Write-Host "Full run log: $runLog" -ForegroundColor DarkGray
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run without -EmailOnly first"
    }
}

Write-Host ""
Write-Host "=== Sending consolidated LEFT FILTER emails (8 scenario rows) ===" -ForegroundColor Yellow

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
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-activitytype-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done - Activity Type continuous-flow run complete." -ForegroundColor Green
Write-Host "  Aggregate dir : test-output/lf-o-activitytype-aggregate"
Write-Host "  Report title  : $reportTitle"
