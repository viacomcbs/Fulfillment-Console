# Resend Job left-filter emails from existing allure-results (no browser rerun).
# Sends TWO emails to Akila only:
#   1) passed scenarios only
#   2) full pass/fail/skip status
#
# Example:
#   .\scripts\send-job-left-filter-email.ps1

param(
    [string] $Email = "Akilandeswari.Sundararajan@paramount.com",
    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "=== Building Job aggregate from allure-results ==="
python scripts/build_job_testng_results_from_allure.py
if ($LASTEXITCODE -ne 0) { throw "Failed to build Job testng-results from allure-results" }

$combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Core8_S09_Job_CombinedEmail_ProdServerSuite.xml"
$reportTitle = "Orders Tab - Left Filters Regression (PROD) - Job type"

Write-Host "=== Sending TWO Job emails (Akil only) ==="
Write-Host "  Stakeholder (passed only): $Email"
Write-Host "  Internal (full status)   : $InternalEmail"

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
Write-Host "Done."
Write-Host "  Aggregate dir : test-output/lf-o-job-aggregate"
Write-Host "  Emails sent to: $Email (x2: passed-only + full status)"
