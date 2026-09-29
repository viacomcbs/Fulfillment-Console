# Job type left filter — run all 8 scenarios (T1 + T2 sessions) -> TWO combined emails at end.
# Includes TC622 Table sync (line-item-type locator fix verified 2026-09-23).
#
# Examples:
#   .\scripts\run-job-left-filter-prod.ps1
#   .\scripts\run-job-left-filter-prod.ps1 -SkipCompile
#   .\scripts\run-job-left-filter-prod.ps1 -Parallel
#   .\scripts\run-job-left-filter-prod.ps1 -EmailOnly -SkipCompile

param(
    [string] $Email = "Akilandeswari.Sundararajan@paramount.com",
    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",
    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,
    [switch] $EmailOnly,
    [switch] $Parallel,
    [switch] $Sequential
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\lf-o-job-aggregate"
$sessionsDir = "src/test/resources/regression/left-filters/orders-view/sessions"
$combinedSuite = "$sessionsDir/LF_O_Core8_S09_Job_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - Job type"

Write-Host "=== Job type Left Filters (8 scenarios) -> TWO emails ===" -ForegroundColor Green
Write-Host "Stakeholder email (passed only) : $Email"
Write-Host "Internal email (full status)    : $InternalEmail"
if ($EmailOnly) {
    Write-Host "Mode                            : Email only (reuse test-output/lf-o-job-aggregate)" -ForegroundColor Yellow
} elseif ($Parallel) {
    Write-Host "Mode                            : Parallel T1 + T2 (2 Synergy browsers)" -ForegroundColor Yellow
} elseif ($Sequential) {
    Write-Host "Mode                            : Single session (LF_O_Core8_S09_Job_All)" -ForegroundColor Yellow
} else {
    Write-Host "Mode                            : Sequential T1 then T2 (2 Synergy sessions)" -ForegroundColor Yellow
}
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

$failedRuns = New-Object System.Collections.Generic.List[string]

function Invoke-JobSession {
    param(
        [string] $RelSuite,
        [string] $AggregateName
    )
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Job session: $AggregateName" -ForegroundColor Cyan
    Write-Host "Suite: $RelSuite" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    mvn -B test `
        "-DsuiteXmlFile=$RelSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (Test-Path $resultsFile) {
        Copy-Item $resultsFile (Join-Path $aggregateDir "$AggregateName-testng-results.xml") -Force
    } else {
        Write-Warning "Missing testng-results.xml after $AggregateName"
    }

    if ($LASTEXITCODE -ne 0) {
        $failedRuns.Add($AggregateName) | Out-Null
        return $false
    }
    return $true
}

if (-not $EmailOnly) {
    if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

    $suite1 = "$sessionsDir/LF_O_Core8_S09_Job_Parallel_Thread1_ProdServerSuite.xml"
    $suite2 = "$sessionsDir/LF_O_Core8_S09_Job_Parallel_Thread2_ProdServerSuite.xml"

    if ($Sequential) {
        $allSuite = "$sessionsDir/LF_O_Core8_S09_Job_All_ProdServerSuite.xml"
        $ok = Invoke-JobSession -RelSuite $allSuite -AggregateName "LF_O_Core8_S09_Job_All"
        if (-not $ok -and $StopOnFirstFailure) {
            throw "Job All session failed"
        }
    } elseif ($Parallel) {
        Write-Host "Starting parallel Job sessions (2 Synergy browsers)..." -ForegroundColor Yellow
        & "$PSScriptRoot\run-job-left-filter-parallel.ps1"
        if ($LASTEXITCODE -ne 0) {
            $failedRuns.Add("LF_O_Core8_S09_Job_Parallel") | Out-Null
            if ($StopOnFirstFailure) { throw "Parallel Job sessions failed" }
        }
        Write-Warning "Parallel mode does not merge per-thread XML — use default (sequential T1+T2) for combined email."
    } else {
        $ok1 = Invoke-JobSession -RelSuite $suite1 -AggregateName "LF_O_Core8_S09_Job_Parallel_Thread1"
        if (-not $ok1 -and $StopOnFirstFailure) { throw "Job T1 session failed" }

        $ok2 = Invoke-JobSession -RelSuite $suite2 -AggregateName "LF_O_Core8_S09_Job_Parallel_Thread2"
        if (-not $ok2 -and $StopOnFirstFailure) { throw "Job T2 session failed" }
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run full script first (without -EmailOnly)"
    }
    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    if ($xmlCount -lt 1) {
        throw "No result XML files in $aggregateDir"
    }
    Write-Host "Skipping Synergy - re-sending emails from $xmlCount saved result file(s)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Sending TWO Job left-filter emails ===" -ForegroundColor Yellow
Write-Host "  1) Stakeholder - passed scenarios only" -ForegroundColor Cyan
Write-Host "  2) Internal    - full pass/fail status" -ForegroundColor Cyan

mvn -B test `
    "-DsuiteXmlFile=$combinedSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-job-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=false"

Write-Host ""
Write-Host "Done - Job type left filters."
Write-Host "  Aggregate dir      : test-output/lf-o-job-aggregate"
Write-Host "  Report title       : $reportTitle"
Write-Host "  Stakeholder email  : $Email (passed only)"
Write-Host "  Internal email     : $InternalEmail (full status)"
if ($failedRuns.Count -gt 0) {
    Write-Host ""
    Write-Host "Sessions with failures (partial results still merged):" -ForegroundColor Yellow
    foreach ($name in $failedRuns) {
        Write-Host "  - $name" -ForegroundColor Yellow
    }
}
