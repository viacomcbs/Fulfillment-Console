# Package Allure report + assertion screenshots for Jira (BSD-29441).
# Run after TestNG suite completes:
#   .\scripts\package-jira-evidence.ps1

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

$JiraId = "BSD-29441"
$Stamp = Get-Date -Format "yyyy-MM-dd_HHmm"
$OutDir = Join-Path $ProjectRoot "test-output\jira-evidence\$Stamp"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

Write-Host "Generating Allure HTML report..."
npx --yes allure-commandline generate allure-results -o allure-report --clean | Out-Null

$AllureZip = Join-Path $OutDir "${JiraId}_AllureReport_$Stamp.zip"
if (Test-Path "allure-report") {
    Compress-Archive -Path "allure-report\*" -DestinationPath $AllureZip -Force
    Write-Host "Allure zip: $AllureZip"
}

# Latest assertion screenshot run folder
$ScreenshotRoot = Join-Path $ProjectRoot "test-output\screenshots"
$LatestRun = Get-ChildItem $ScreenshotRoot -Directory -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1

$ScreensZip = Join-Path $OutDir "${JiraId}_AssertionScreenshots_$Stamp.zip"
if ($LatestRun) {
    $AssertionDir = Join-Path $LatestRun.FullName "assertion"
    if (Test-Path $AssertionDir) {
        Compress-Archive -Path "$AssertionDir\*" -DestinationPath $ScreensZip -Force
        Write-Host "Screenshots zip: $ScreensZip"
    }
}

# Parse Allure result JSON for pass/fail summary
$ResultsDir = Join-Path $ProjectRoot "allure-results"
$Tests = @()
Get-ChildItem $ResultsDir -Filter "*-result.json" -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        $j = Get-Content $_.FullName -Raw | ConvertFrom-Json
        if ($j.name) {
            $Tests += [PSCustomObject]@{
                Name   = $j.name
                Status = $j.status
                Full   = $j.fullName
            }
        }
    } catch { }
}

$Passed = ($Tests | Where-Object { $_.Status -eq "passed" }).Count
$Failed = ($Tests | Where-Object { $_.Status -eq "failed" }).Count
$Skipped = ($Tests | Where-Object { $_.Status -eq "skipped" }).Count
$Broken = ($Tests | Where-Object { $_.Status -eq "broken" }).Count

$SummaryPath = Join-Path $OutDir "${JiraId}_TestEvidenceSummary.txt"
$Summary = @"
$JiraId — PTS Packaging ID (Orders) — PROD automation evidence
Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

Environment: PROD (Synergy Public Devices)
Application: Fulfillment Console — Orders tab, Demand system = PTS, Yesterday date range

Suite XML: src/test/resources/FF_PTS_BSD29441_ProdServerSuite.xml
Tests in suite: O_001, O_011, O_009, O_015 (O_013 export skipped)

Results (from allure-results):
  Passed:  $Passed
  Failed:  $Failed
  Skipped: $Skipped
  Broken:  $Broken

Individual tests:
$(
    ($Tests | Sort-Object Name | ForEach-Object { "  [$($_.Status.ToUpper())] $($_.Name)" }) -join "`n"
)

Attachments for Jira:
  1. ${JiraId}_AllureReport_$Stamp.zip — open index.html inside for full report
  2. ${JiraId}_AssertionScreenshots_$Stamp.zip — step screenshots per test

Local paths:
  Allure HTML: $ProjectRoot\allure-report\index.html
  Evidence folder: $OutDir

Suggested Jira comment:
---
PROD automation completed for $JiraId (PTS Packaging ID on Orders).
Filter: Yesterday + Demand system = PTS. Export test (O_013) skipped.
See attached Allure report and assertion screenshots.
---
"@
Set-Content -Path $SummaryPath -Value $Summary -Encoding UTF8
Write-Host ""
Write-Host $Summary
Write-Host ""
Write-Host "Done. Attach files from: $OutDir"
