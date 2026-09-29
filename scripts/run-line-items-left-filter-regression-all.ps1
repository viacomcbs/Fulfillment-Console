# Run consolidated Line Items-view left-filter regression (one suite, one email).
#
# Example:
#   .\scripts\run-line-items-left-filter-regression-all.ps1 -Email "you@paramount.com"

param(
    [Parameter(Mandatory = $true)]
    [string] $Email,

    [ValidateSet("PROD", "UAT", "DEV")]
    [string] $Environment = "PROD"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$suite = "src/test/resources/regression/left-filters/line-items-view/LF_LI_All_LeftFilters_ProdServerSuite.xml"

Write-Host "Running consolidated Line Items left-filter regression"
Write-Host "Suite: $suite"
Write-Host "Environment: $Environment"
Write-Host "Email: $Email"

mvn -B test `
    "-DsuiteXmlFile=$suite" `
    "-Dsystem.test.testenvironment=$Environment" `
    "-Dsystem.test.sendreportautoemails=true" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.leftfilteremailsuitetitle=Line Items Tab — All left filters regression ($Environment)"

Write-Host "Done. Expect ONE email with all scenario results."
