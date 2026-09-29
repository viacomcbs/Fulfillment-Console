# Runs Job left-filter tests in parallel using 2 JVMs (2 Synergy browser sessions).
# Do NOT use TestNG parallel="classes" in one suite — LeftFilterSessionHelper uses shared static state.
#
# Usage:
#   cd C:\FulfillmentConsole\Fulfillment-Console
#   .\scripts\run-job-left-filter-parallel.ps1
#
# Optional: skip compile if already built
#   .\scripts\run-job-left-filter-parallel.ps1 -CompileFirst:$false

param(
    [bool] $CompileFirst = $true
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$suite1 = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Core8_S09_Job_Parallel_Thread1_ProdServerSuite.xml"
$suite2 = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Core8_S09_Job_Parallel_Thread2_ProdServerSuite.xml"

$logDir = Join-Path $repoRoot "test-output\parallel-logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$log1 = Join-Path $logDir "job-thread1_$stamp.log"
$log2 = Join-Path $logDir "job-thread2_$stamp.log"

if ($CompileFirst) {
    Write-Host "Compiling tests..."
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

Write-Host "Starting Job left-filter parallel run (2 threads)..."
Write-Host "  Thread 1 log: $log1"
Write-Host "  Thread 2 log: $log2"

$job1 = Start-Job -ScriptBlock {
    param($Root, $Suite, $Log)
    Set-Location $Root
    & mvn -B test "-DsuiteXmlFile=$Suite" *>&1 | Out-File -FilePath $Log -Encoding utf8
    return $LASTEXITCODE
} -ArgumentList $repoRoot, $suite1, $log1

$job2 = Start-Job -ScriptBlock {
    param($Root, $Suite, $Log)
    Set-Location $Root
    & mvn -B test "-DsuiteXmlFile=$Suite" *>&1 | Out-File -FilePath $Log -Encoding utf8
    return $LASTEXITCODE
} -ArgumentList $repoRoot, $suite2, $log2

Wait-Job $job1, $job2 | Out-Null
$exit1 = Receive-Job $job1
$exit2 = Receive-Job $job2
Remove-Job $job1, $job2 -Force

Write-Host ""
Write-Host "Thread 1 exit: $exit1"
Write-Host "Thread 2 exit: $exit2"

if ($exit1 -ne 0 -or $exit2 -ne 0) {
    Write-Host "One or both threads failed. Check logs in $logDir"
    exit 1
}

Write-Host "All Job tests passed (both threads)."
exit 0
