# Orders left filters - TWO Synergy threads in parallel:
#   Thread 1: filters executed at least once (from perfilter_execution_history.json)
#   Thread 2: filters never executed yet
# Each thread runs one filter session at a time (sequential within thread).
# At end: ONE consolidated email (stakeholder passed-only + internal full status).
#
# Examples:
#   .\scripts\run-lf-orders-split-parallel-prod.ps1
#   .\scripts\run-lf-orders-split-parallel-prod.ps1 -SkipCompile
#   .\scripts\run-lf-orders-split-parallel-prod.ps1 -EmailOnly -SkipCompile
#   .\scripts\run-lf-orders-split-parallel-prod.ps1 -Sequential

param(
    [string] $Email = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $StopOnFirstFailure,
    [switch] $EmailOnly,
    [switch] $Sequential,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$aggregateDir = Join-Path $repoRoot "test-output\lf-o-split-aggregate"
$manifestPath = Join-Path $repoRoot "test-output\lf-o-split-manifest.json"
$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Split_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD)"
$logDir = Join-Path $repoRoot "test-output\parallel-logs"

Write-Host "=== Orders Left Filters - 2-thread split run -> consolidated email ===" -ForegroundColor Green
Write-Host "Thread 1 : filters previously executed (from history)"
Write-Host "Thread 2 : filters never executed yet"
Write-Host "Stakeholder email (passed only) : $Email"
Write-Host "Internal email (full status)    : $InternalEmail"
if ($EmailOnly) {
    Write-Host "Mode                            : Email only (reuse test-output/lf-o-split-aggregate)" -ForegroundColor Yellow
} elseif ($Sequential) {
    Write-Host "Mode                            : Sequential (T1 then T2, single browser)" -ForegroundColor Yellow
} else {
    Write-Host "Mode                            : Parallel (2 Synergy browsers)" -ForegroundColor Yellow
}
Write-Host ""

if (-not $EmailOnly) {
    Write-Host "=== Building split-thread suites + manifest ===" -ForegroundColor Cyan
    python scripts/build_lf_o_split_thread_suites.py
    if ($LASTEXITCODE -ne 0) { throw "build_lf_o_split_thread_suites.py failed" }
}

if (-not (Test-Path $manifestPath)) {
    throw "Manifest missing: $manifestPath - run without -EmailOnly first"
}

$manifest = Get-Content $manifestPath -Raw | ConvertFrom-Json
$thread1 = @($manifest.thread1ExecutedAtLeastOnce)
$thread2 = @($manifest.thread2NeverExecuted)

Write-Host ""
Write-Host "Split summary:" -ForegroundColor Cyan
Write-Host "  Thread 1 filters : $($thread1.Count) ($($manifest.summary.thread1Tests) tests)"
Write-Host "  Thread 2 filters : $($thread2.Count) ($($manifest.summary.thread2Tests) tests)"
Write-Host ""

if (-not $SkipCompile -and -not $EmailOnly) {
    Write-Host "=== Compiling tests ===" -ForegroundColor Cyan
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

$failedRuns = New-Object System.Collections.Generic.List[string]

function Invoke-FilterThread {
    param(
        [string] $ThreadName,
        [array] $Filters,
        [string] $AggregatePrefix
    )

    $localFailed = New-Object System.Collections.Generic.List[string]
    $reportsDir = "test-output/surefire-reports-$AggregatePrefix"
    $num = 0

    foreach ($filter in $Filters) {
        $num++
        $suiteRel = $filter.suiteFile
        $baseName = $filter.suiteBaseName
        $label = $filter.filterLabel

        Write-Host ""
        Write-Host "[$ThreadName] Filter $num/$($Filters.Count): $label ($($filter.testCount) tests)" -ForegroundColor Cyan
        Write-Host "  Suite: $suiteRel"

        mvn -B test `
            "-DsuiteXmlFile=$suiteRel" `
            "-Dsurefire.reportsDirectory=$reportsDir" `
            "-Dsystem.test.testenvironment=PROD" `
            "-Dsystem.test.sendreportautoemails=false" `
            "-Dsystem.test.sendchatreport=false" `
            "-Dsystem.test.userkey=$akilaUserKey" `
            "-Dsystem.test.clientid=$akilaClientId"

        $resultsFile = Join-Path $repoRoot "$reportsDir\testng-results.xml"
        $destName = "${AggregatePrefix}_${baseName}-testng-results.xml"
        $destPath = Join-Path $aggregateDir $destName

        if (Test-Path $resultsFile) {
            Copy-Item $resultsFile $destPath -Force
            Write-Host "  Saved: $destName" -ForegroundColor DarkGray
        } else {
            Write-Warning "[$ThreadName] Missing testng-results.xml after $label"
        }

        if ($LASTEXITCODE -ne 0) {
            $localFailed.Add("$ThreadName|$label|$suiteRel") | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }

    return $localFailed
}

if (-not $EmailOnly) {
    if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
    New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null
    New-Item -ItemType Directory -Force -Path $logDir | Out-Null

    if ($Sequential) {
        Write-Host "=== Running Thread 1 (executed filters) sequentially ===" -ForegroundColor Yellow
        $f1 = Invoke-FilterThread -ThreadName "T1" -Filters $thread1 -AggregatePrefix "T1"
        foreach ($f in $f1) { $failedRuns.Add($f) | Out-Null }
        if ($StopOnFirstFailure -and $f1.Count -gt 0) {
            throw "Thread 1 stopped on first failure"
        }

        Write-Host ""
        Write-Host "=== Running Thread 2 (never-executed filters) sequentially ===" -ForegroundColor Yellow
        $f2 = Invoke-FilterThread -ThreadName "T2" -Filters $thread2 -AggregatePrefix "T2"
        foreach ($f in $f2) { $failedRuns.Add($f) | Out-Null }
    } else {
        $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $log1 = Join-Path $logDir "lf-split-thread1_$stamp.log"
        $log2 = Join-Path $logDir "lf-split-thread2_$stamp.log"

        Write-Host "=== Starting parallel threads (2 Synergy browsers) ===" -ForegroundColor Yellow
        Write-Host "  Thread 1 log: $log1"
        Write-Host "  Thread 2 log: $log2"

        $job1 = Start-Job -ScriptBlock {
            param($Root, $FiltersJson, $Prefix, $UserKey, $ClientId, $Log)
            Set-Location $Root
            $filters = $FiltersJson | ConvertFrom-Json
            $agg = Join-Path $Root "test-output\lf-o-split-aggregate"
            $reportsDir = "test-output/surefire-reports-$Prefix"
            $failed = @()
            $n = 0
            foreach ($filter in $filters) {
                $n++
                $suiteRel = $filter.suiteFile
                $baseName = $filter.suiteBaseName
                $label = $filter.filterLabel
                Add-Content -Path $Log -Value "[$n/$($filters.Count)] $label -> $suiteRel"
                & mvn -B test `
                    "-DsuiteXmlFile=$suiteRel" `
                    "-Dsurefire.reportsDirectory=$reportsDir" `
                    "-Dsystem.test.testenvironment=PROD" `
                    "-Dsystem.test.sendreportautoemails=false" `
                    "-Dsystem.test.sendchatreport=false" `
                    "-Dsystem.test.userkey=$UserKey" `
                    "-Dsystem.test.clientid=$ClientId" 2>&1 | Add-Content -Path $Log
                $resultsFile = Join-Path $Root "$reportsDir\testng-results.xml"
                $dest = Join-Path $agg "${Prefix}_${baseName}-testng-results.xml"
                if (Test-Path $resultsFile) { Copy-Item $resultsFile $dest -Force }
                if ($LASTEXITCODE -ne 0) { $failed += "$label" }
            }
            return ,$failed
        } -ArgumentList $repoRoot, ($thread1 | ConvertTo-Json -Depth 5 -Compress), "T1", $akilaUserKey, $akilaClientId, $log1

        $job2 = Start-Job -ScriptBlock {
            param($Root, $FiltersJson, $Prefix, $UserKey, $ClientId, $Log)
            Set-Location $Root
            $filters = $FiltersJson | ConvertFrom-Json
            $agg = Join-Path $Root "test-output\lf-o-split-aggregate"
            $reportsDir = "test-output/surefire-reports-$Prefix"
            $failed = @()
            $n = 0
            foreach ($filter in $filters) {
                $n++
                $suiteRel = $filter.suiteFile
                $baseName = $filter.suiteBaseName
                $label = $filter.filterLabel
                Add-Content -Path $Log -Value "[$n/$($filters.Count)] $label -> $suiteRel"
                & mvn -B test `
                    "-DsuiteXmlFile=$suiteRel" `
                    "-Dsurefire.reportsDirectory=$reportsDir" `
                    "-Dsystem.test.testenvironment=PROD" `
                    "-Dsystem.test.sendreportautoemails=false" `
                    "-Dsystem.test.sendchatreport=false" `
                    "-Dsystem.test.userkey=$UserKey" `
                    "-Dsystem.test.clientid=$ClientId" 2>&1 | Add-Content -Path $Log
                $resultsFile = Join-Path $Root "$reportsDir\testng-results.xml"
                $dest = Join-Path $agg "${Prefix}_${baseName}-testng-results.xml"
                if (Test-Path $resultsFile) { Copy-Item $resultsFile $dest -Force }
                if ($LASTEXITCODE -ne 0) { $failed += "$label" }
            }
            return ,$failed
        } -ArgumentList $repoRoot, ($thread2 | ConvertTo-Json -Depth 5 -Compress), "T2", $akilaUserKey, $akilaClientId, $log2

        Wait-Job $job1, $job2 | Out-Null
        $fail1 = @(Receive-Job $job1)
        $fail2 = @(Receive-Job $job2)
        Remove-Job $job1, $job2 -Force

        foreach ($f in $fail1) { $failedRuns.Add("T1|$f") | Out-Null }
        foreach ($f in $fail2) { $failedRuns.Add("T2|$f") | Out-Null }

        Write-Host ""
        Write-Host "Thread 1 failures: $($fail1.Count)" -ForegroundColor $(if ($fail1.Count -gt 0) { "Yellow" } else { "Green" })
        Write-Host "Thread 2 failures: $($fail2.Count)" -ForegroundColor $(if ($fail2.Count -gt 0) { "Yellow" } else { "Green" })
    }
} else {
    if (-not (Test-Path $aggregateDir)) {
        throw "Aggregate dir missing: $aggregateDir - run full script first (without -EmailOnly)"
    }
    $xmlCount = @(Get-ChildItem $aggregateDir -Filter "*.xml").Count
    if ($xmlCount -lt 1) {
        throw "No result XML files in $aggregateDir"
    }
    Write-Host "Skipping Synergy - re-sending email from $xmlCount saved result file(s)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Sending consolidated LEFT FILTER emails ===" -ForegroundColor Yellow
Write-Host "  1) Stakeholder - passed scenarios only" -ForegroundColor Cyan
Write-Host "  2) Internal    - full pass/fail status" -ForegroundColor Cyan

$slackArgs = @()
if ($env:SLACK_WEBHOOK_URL) {
    $slackArgs += "-Dsystem.test.slackwebhookurl=$($env:SLACK_WEBHOOK_URL)"
} else {
    Write-Warning "SLACK_WEBHOOK_URL is not set - Slack API post may be skipped."
}

mvn -B test `
    "-DsuiteXmlFile=$combinedSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=test-output/lf-o-split-aggregate" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=true" `
    "-Dsystem.test.slackchannel=$SlackChannel" `
    @slackArgs

Write-Host ""
Write-Host "Done - Orders left-filter split parallel run." -ForegroundColor Green
Write-Host "  Manifest           : test-output/lf-o-split-manifest.json"
Write-Host "  Aggregate dir      : test-output/lf-o-split-aggregate"
Write-Host "  Report title       : $reportTitle"
Write-Host "  Stakeholder email  : $Email"
Write-Host "  Internal email     : $InternalEmail"
if ($failedRuns.Count -gt 0) {
    Write-Host ""
    Write-Host "Filters with Maven failures (partial results still merged into email):" -ForegroundColor Yellow
    foreach ($name in $failedRuns) {
        Write-Host "  - $name" -ForegroundColor Yellow
    }
}
