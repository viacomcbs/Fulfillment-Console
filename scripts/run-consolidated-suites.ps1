# Run multiple suites and send ONE consolidated email report at the end.
# Usage: .\scripts\run-consolidated-suites.ps1

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

$commonArgs = @(
    "-Dsystem.test.consolidatereportemail=true",
    "-Dsystem.test.reportbatchid=ff-regression"
)

$suites = @(
    @{ File = "src/test/resources/SortSuiteConfig.xml"; Reset = $true; Finalize = $false },
    @{ File = "src/test/resources/ColumnsSuiteConfig.xml"; Reset = $false; Finalize = $false },
    @{ File = "src/test/resources/ColumnSearchSuiteConfig.xml"; Reset = $false; Finalize = $true }
)

foreach ($suite in $suites) {
    $args = @("test", "-DsuiteXmlFile=$($suite.File)") + $commonArgs
    if ($suite.Reset) {
        $args += "-Dsystem.test.resetconsolidatedreport=true"
    }
    if ($suite.Finalize) {
        $args += "-Dsystem.test.finalizeconsolidatedreport=true"
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Running suite: $($suite.File)" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    mvn @args
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Suite failed: $($suite.File)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Consolidated report email sent after the final suite (if FinalizeConsolidatedReport=true)." -ForegroundColor Green
