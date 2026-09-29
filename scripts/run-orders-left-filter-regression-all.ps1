# Run consolidated Orders-view left-filter regression (one suite, one email).
#
# Example:
#   .\scripts\run-orders-left-filter-regression-all.ps1 -Email "you@paramount.com"

param(
    [Parameter(Mandatory = $true)]
    [string] $Email,

    [ValidateSet("PROD", "UAT", "DEV")]
    [string] $Environment = "PROD"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$suite = "src/test/resources/regression/left-filters/orders-view/LF_O_Core8_LeftFilters_ProdServerSuite.xml"

Write-Host "Running consolidated Orders left-filter regression"
Write-Host "Suite: $suite"
Write-Host "Environment: $Environment"
Write-Host "Email: $Email"

mvn -B test `
    "-DsuiteXmlFile=$suite" `
    "-Dsystem.test.testenvironment=$Environment" `
    "-Dsystem.test.sendreportautoemails=true" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.leftfilteremailsuitetitle=Orders Tab — Core 8 left filters regression ($Environment)"

Write-Host "Done. Expect ONE email with all scenario results."
