# Email-only: 1 PASS (LF_O_TC122) + 1 FAIL (LF_O_TC999) report preview — no browser rerun.
#
# Example:
#   .\scripts\send-job-report-preview-email.ps1
#   .\scripts\send-job-report-preview-email.ps1 -Email "you@paramount.com"

param(
    [string] $Email = "Akilandeswari.Sundararajan@paramount.com",
    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$aggregateDir = "test-output/lf-o-job-report-preview-aggregate"
$aggregateFile = Join-Path $aggregateDir "Job_ReportPreview-testng-results.xml"
if (-not (Test-Path $aggregateFile)) {
    throw "Missing preview aggregate: $aggregateFile"
}

$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Core8_S09_Job_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - Job type - Report Preview"

Write-Host "=== Sending report preview emails (1 pass + 1 fail, no Synergy) ===" -ForegroundColor Green
Write-Host "  Stakeholder (passed only): $Email"
Write-Host "  Internal (full status)   : $InternalEmail"

mvn -B test `
    "-DsuiteXmlFile=$combinedSuite" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.aggregateresultsdir=$aggregateDir" `
    "-Dsystem.test.sendreportautoemails=false" `
    "-Dsystem.test.sendchatreport=false"

Write-Host ""
Write-Host "Done. Preview aggregate: $aggregateDir"
