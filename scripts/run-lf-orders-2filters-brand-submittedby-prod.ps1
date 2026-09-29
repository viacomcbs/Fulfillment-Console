# Orders left filters - 2 filters (Brand + Submitted By), single Synergy session.
# Login -> Yesterday -> Manage columns once (Brand + Submitted By) -> test immediately.
# Per filter: Basic -> Option order -> Scroll -> Search -> Collapse -> Table sync -> Active -> Clear -> Select all.
# Between filters: refresh + verify Clear filters is NOT active.
# After run: consolidated email (stakeholder = passed only, internal = full pass/fail).
#
# Examples:
#   .\scripts\run-lf-orders-2filters-brand-submittedby-prod.ps1
#   .\scripts\run-lf-orders-2filters-brand-submittedby-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-2filters-brand-submittedby-prod.ps1 -EmailOnly -SkipCompile

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),
    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",
    [switch] $SkipCompile,
    [switch] $SkipEmail,
    [switch] $EmailOnly,
    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$batchSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_BrandSubmittedBy_2filters_ProdServerSuite.xml"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_BrandSubmittedBy_2filters_CombinedEmail_ProdServerSuite.xml"
$aggregateDir = Join-Path $repoRoot "test-output\lf-o-2filter-brand-submittedby-aggregate"
$reportTitle = "Orders Tab - Left Filters (PROD) - Brand + Submitted By"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"

Write-Host "=== Orders Left Filters - Brand + Submitted By (2 filters) ===" -ForegroundColor Green
Write-Host "Mode    : 1 login, Manage columns once, refresh between filters" -ForegroundColor Yellow
Write-Host "Steps   : 9 per filter (collapse after search)" -ForegroundColor Yellow
Write-Host "Email   : stakeholder (passed only) + internal (full pass/fail)" -ForegroundColor Yellow
Write-Host "Suite   : $batchSuite"
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

$batchExitCode = 0

if (-not $EmailOnly) {
    Write-Host "=== Pre-flight: Synergy server check ===" -ForegroundColor Cyan
    if (-not (Test-SynergyServerReachable)) {
        throw "Synergy server is not reachable. Check network/VPN, then re-run."
    }
    Write-Host ""

    New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $runLog = Join-Path $logDir "lf-2filter-brand-submittedby_$stamp.log"

    Write-Host "=== Running 2-filter batch ===" -ForegroundColor Cyan
    Write-Host "  Log: $runLog" -ForegroundColor DarkGray
    Write-Host ""

    Write-Host "  (Live console + transcript log - do not pipe mvn to Tee-Object)" -ForegroundColor DarkGray
    Start-Transcript -Path $runLog -Append
    try {
        mvn -B test `
            "-DsuiteXmlFile=$batchSuite" `
            "-Dsystem.test.testenvironment=PROD" `
            "-Dsystem.test.sendreportautoemails=false" `
            "-Dsystem.test.sendchatreport=false" `
            "-Dsystem.test.userkey=$akilaUserKey" `
            "-Dsystem.test.clientid=$akilaClientId"
        $batchExitCode = $LASTEXITCODE
    } finally {
        Stop-Transcript
    }
    Write-Host ""
    Write-Host "Full run log: $runLog" -ForegroundColor DarkGray

    $snapshot = Join-Path $aggregateDir "lf-recorded-snapshot.xml"
    if (Test-Path $snapshot) {
        Write-Host "Aggregate snapshot: $snapshot" -ForegroundColor Green
    } else {
        Write-Warning "Missing lf-recorded-snapshot.xml - email may have no scenario rows"
    }

    if ($batchExitCode -ne 0) {
        Write-Host "Batch run finished with failures (exit $batchExitCode)" -ForegroundColor Yellow
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run without -EmailOnly first"
    }
}

if (-not $SkipEmail) {
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
        "-Dsystem.test.aggregateresultsdir=test-output/lf-o-2filter-brand-submittedby-aggregate" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=true" `
        "-Dsystem.test.slackchannel=$SlackChannel" `
        @slackArgs

    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Consolidated email step failed (exit $LASTEXITCODE)"
        if ($batchExitCode -eq 0) { exit $LASTEXITCODE }
    }
}

Write-Host ""
if ($batchExitCode -ne 0) {
    Write-Host "Done - batch had failures; check internal email for FAIL rows." -ForegroundColor Yellow
    exit $batchExitCode
}

Write-Host "Done - Brand + Submitted By batch complete." -ForegroundColor Green
