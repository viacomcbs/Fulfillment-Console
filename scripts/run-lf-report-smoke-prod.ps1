# Quick left-filter report smoke on PROD Synergy (7 Activity Type tests + email + PDF).
#
# Example:
#   .\scripts\run-lf-report-smoke-prod.ps1
#   .\scripts\run-lf-report-smoke-prod.ps1 -Email "you@paramount.com"

param(
    [string] $Email = "Akilandeswari.Sundararajan@paramount.com"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$suite = "src/test/resources/regression/left-filters/orders-view/LF_O_ReportSmoke_ActivityType_ProdServerSuite.xml"

Write-Host "=== LF report smoke (Activity Type, PROD Synergy) ==="
Write-Host "Suite: $suite"
Write-Host "Email: $Email"
Write-Host ""

Write-Host "=== Compiling tests ==="
mvn -B test-compile

Write-Host "=== Running 7 Activity Type tests ==="
mvn -B test `
    "-DsuiteXmlFile=$suite" `
    "-Dsystem.test.sendreportemailaddress=$Email"

Write-Host ""
Write-Host "Done. Check your inbox for the automation email and test-output/reports/ for the PDF."
