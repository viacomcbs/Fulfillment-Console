# Full PROD regression: ALL left-filter tests (Orders + Line Items) + ALL table-view suites.
#
# Dual email (default ON):
#   Stakeholder - passed scenarios only -> Akila + Slack email-in
#   Internal    - full pass/fail status  -> Akila only
#
# Left filters (2 consolidated suites):
#   Orders tab + Line Items tab - 9 filters each (incl. Error Message)
#
# Table view (13 SuiteConfig XMLs):
#   Manage columns, sort, search, line-item tab, BSD-29174/29302/29791/30034
#
# Examples:
#   .\scripts\run-full-lf-and-tableview-regression-prod.ps1
#   .\scripts\run-full-lf-and-tableview-regression-prod.ps1 -SkipTableView
#   .\scripts\run-full-lf-and-tableview-regression-prod.ps1 -SendFinalDualSummary -CleanAllureResults

param(
    [string] $StakeholderEmail = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),

    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",

    [ValidateSet("PROD", "UAT", "DEV")]
    [string] $Environment = "PROD",

    [switch] $SkipLeftFilters,
    [switch] $SkipTableView,
    [switch] $SkipCompile,
    [switch] $DisableDualEmail,

    [switch] $SendTableViewEmails,
    [switch] $SendFinalDualSummary,
    [switch] $StopOnFirstFailure,
    [switch] $CleanAllureResults,

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$leftFilterSuites = @(
    @{
        File  = "src/test/resources/regression/left-filters/orders-view/LF_O_All_LeftFilters_ProdServerSuite.xml"
        Title = "Orders Tab - All left filters regression ($Environment)"
    },
    @{
        File  = "src/test/resources/regression/left-filters/line-items-view/LF_LI_All_LeftFilters_ProdServerSuite.xml"
        Title = "Line Items Tab - All left filters regression ($Environment)"
    }
)

$tableViewSuites = @(
    @{ File = "src/test/resources/regression/table-view/ColumnsSuiteConfig.xml"; Title = "Manage Columns (TC_011-TC_126)" },
    @{ File = "src/test/resources/regression/table-view/SortSuiteConfig.xml"; Title = "Column Sort" },
    @{ File = "src/test/resources/regression/table-view/ColumnSearchSuiteConfig.xml"; Title = "Column Search" },
    @{ File = "src/test/resources/regression/table-view/ColumnSearchNegativeSuiteConfig.xml"; Title = "Column Search Negative" },
    @{ File = "src/test/resources/regression/table-view/LineItemTabColumnsSuiteConfig.xml"; Title = "Line Item Tab - Manage Columns" },
    @{ File = "src/test/resources/regression/table-view/LineItemTabSuiteConfig.xml"; Title = "Line Item Tab - Sort / Search / Export" },
    @{ File = "src/test/resources/regression/table-view/BSD29174SuiteConfig.xml"; Title = "BSD-29174 Order Status Tooltip" },
    @{ File = "src/test/resources/regression/table-view/BSD29174CountMatchSuiteConfig.xml"; Title = "BSD-29174 Status Count Match" },
    @{ File = "src/test/resources/regression/table-view/BSD29302SuiteConfig.xml"; Title = "BSD-29302 Package Delivered Column" },
    @{ File = "src/test/resources/regression/table-view/BSD29791SuiteConfig.xml"; Title = "BSD-29791 File Size Label" },
    @{ File = "src/test/resources/regression/table-view/BSD30034SuiteConfig.xml"; Title = "BSD-30034 Search + Export" },
    @{ File = "src/test/resources/regression/table-view/BSD30034SearchOnlySuiteConfig.xml"; Title = "BSD-30034 Search Only" },
    @{ File = "src/test/resources/regression/table-view/BSD30034ExportSuiteConfig.xml"; Title = "BSD-30034 Export Only" }
)

function Invoke-MvnSuite {
    param(
        [string] $SuiteFile,
        [string] $SuiteTitle,
        [bool]   $SendEmail,
        [bool]   $IsLeftFilter
    )

    $args = @(
        "-B", "test",
        "-DsuiteXmlFile=$SuiteFile",
        "-Dsystem.test.testenvironment=$Environment",
        "-Dsystem.test.userkey=$akilaUserKey",
        "-Dsystem.test.clientid=$akilaClientId",
        "-Dsystem.test.sendreportautoemails=$($SendEmail.ToString().ToLower())",
        "-Dsystem.test.sendreportemailaddress=$StakeholderEmail",
        "-Dsystem.test.uploadpdf=true",
        "-Dsystem.test.attachpdfreport=true"
    )

    if (-not $DisableDualEmail) {
        $args += "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail"
    }

    if ($IsLeftFilter) {
        $args += "-Dsystem.test.leftfilteremailreport=true"
        $args += "-Dsystem.test.leftfilteremailsuitetitle=$SuiteTitle"
    } else {
        $args += "-Dsystem.test.sendchatreport=false"
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Running: $SuiteTitle" -ForegroundColor Cyan
    Write-Host "Suite  : $SuiteFile" -ForegroundColor Cyan
    if ($SendEmail) {
        Write-Host "Email  : stakeholder (passed-only) -> $StakeholderEmail" -ForegroundColor Cyan
        if (-not $DisableDualEmail) {
            Write-Host "Email  : internal (full status)   -> $InternalEmail" -ForegroundColor Cyan
        }
    } else {
        Write-Host "Email  : (suppressed during run)" -ForegroundColor Cyan
    }
    Write-Host "========================================" -ForegroundColor Cyan

    mvn @args
    return $LASTEXITCODE
}

$failedSuites = New-Object System.Collections.Generic.List[string]
$passedSuites = New-Object System.Collections.Generic.List[string]
$totalSuites = 0

Write-Host "=== Full LF + Table-View PROD Regression ===" -ForegroundColor Green
Write-Host "Environment        : $Environment"
Write-Host "Dual email         : $(if ($DisableDualEmail) { 'OFF' } else { 'ON' })"
Write-Host "Stakeholder email  : $StakeholderEmail"
Write-Host "Internal email     : $InternalEmail"
Write-Host "LF suites          : $(if ($SkipLeftFilters) { 'skipped' } else { $leftFilterSuites.Count })"
Write-Host "TV suites          : $(if ($SkipTableView) { 'skipped' } else { $tableViewSuites.Count })"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ==="
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

if ($CleanAllureResults) {
    $allureDir = Join-Path $repoRoot "allure-results"
    if (Test-Path $allureDir) {
        Write-Host "Cleaning allure-results..."
        Remove-Item -Recurse -Force $allureDir
    }
}

if (-not $SkipLeftFilters) {
    Write-Host ""
    Write-Host "=== LEFT FILTERS ($($leftFilterSuites.Count) consolidated suites) ===" -ForegroundColor Yellow
    foreach ($suite in $leftFilterSuites) {
        $totalSuites++
        $code = Invoke-MvnSuite -SuiteFile $suite.File -SuiteTitle $suite.Title -SendEmail $true -IsLeftFilter $true
        if ($code -eq 0) {
            $passedSuites.Add($suite.Title) | Out-Null
        } else {
            $failedSuites.Add("$($suite.Title) [$($suite.File)]") | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }
}

if (-not $SkipTableView -and (-not $StopOnFirstFailure -or $failedSuites.Count -eq 0)) {
    Write-Host ""
    Write-Host "=== TABLE VIEW ($($tableViewSuites.Count) suites) ===" -ForegroundColor Yellow
    foreach ($suite in $tableViewSuites) {
        $totalSuites++
        $sendEmail = [bool]$SendTableViewEmails
        $code = Invoke-MvnSuite -SuiteFile $suite.File -SuiteTitle $suite.Title -SendEmail $sendEmail -IsLeftFilter $false
        if ($code -eq 0) {
            $passedSuites.Add($suite.Title) | Out-Null
        } else {
            $failedSuites.Add("$($suite.Title) [$($suite.File)]") | Out-Null
            if ($StopOnFirstFailure) { break }
        }
    }
}

if ($SendFinalDualSummary) {
    Write-Host ""
    Write-Host "=== Sending final dual summary (passed-only + full status) ===" -ForegroundColor Yellow
    $summaryTitle = "Full LF + Table-View Regression ($Environment)"
    $summaryArgs = @(
        "-B", "test-compile", "exec:java",
        "-Dexec.mainClass=com.paramount.test.ff.common.util.reporting.TodayPassedReportSender",
        "-Dexec.classpathScope=test",
        "-Dexec.args=$summaryTitle",
        "-Dsystem.test.sendreportautoemails=true",
        "-Dsystem.test.sendreportemailaddress=$StakeholderEmail"
    )
    if (-not $DisableDualEmail) {
        $summaryArgs += "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail"
    }
    mvn @summaryArgs
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "REGRESSION RUN COMPLETE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Total suites attempted : $totalSuites"
Write-Host "Passed                 : $($passedSuites.Count)"
Write-Host "Failed                 : $($failedSuites.Count)"
if ($failedSuites.Count -gt 0) {
    Write-Host ""
    Write-Host "Failed suites:" -ForegroundColor Red
    foreach ($name in $failedSuites) {
        Write-Host "  - $name" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "All suites passed."
