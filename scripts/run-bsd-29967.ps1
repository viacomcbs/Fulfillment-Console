# BSD-29967 — In-sprint automation on UAT (or DEV/PROD).
# Accumulates workflow results locally; Jira post is separate (see docs/automation/BSD-29967_UAT_Run_And_Jira_Post.md).
#
# Example:
#   .\scripts\run-bsd-29967.ps1
#   .\scripts\run-bsd-29967.ps1 -Environment UAT -Runs 4 -Email "you@paramount.com"

param(
    [ValidateSet("PROD", "UAT", "DEV")]
    [string] $Environment = "UAT",

    [int] $Runs = 1,

    [string] $Email = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $PostToJira
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$suite = if ($Environment -eq "UAT") {
    "src/test/resources/insprint-automation/BSD-29967_UAT_ProdServerSuite.xml"
} else {
    "src/test/resources/insprint-automation/BSD-29967_ProdServerSuite.xml"
}

for ($i = 1; $i -le $Runs; $i++) {
    Write-Host ""
    Write-Host "=== BSD-29967 run $i of $Runs ($Environment) ==="
    mvn -B test `
        "-DsuiteXmlFile=$suite" `
        "-Dsystem.test.testenvironment=$Environment" `
        "-Dsystem.test.sendreportemailaddress=$Email" `
        "-Dsystem.test.attachjiraevidenceonpass=false"
}

if ($PostToJira) {
    Write-Host ""
    Write-Host "=== Posting accumulated evidence to Jira ==="
    mvn -q exec:java "-Dexec.classpathScope=test" `
        "-Dexec.mainClass=com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967JiraManualPost"
}

Write-Host ""
Write-Host "Done. Accumulated store: test-output/jira-evidence/BSD-29967_workflow-results-accumulated.json"
if (-not $PostToJira) {
    Write-Host "To post to Jira after 3-4 runs: .\scripts\run-bsd-29967.ps1 -PostToJira"
}
