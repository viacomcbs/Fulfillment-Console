# BSD-30019 - In-sprint automation: 3 Synergy sessions + 1 combined email (22 scenarios).
#
# Session 1: Orders per-filter (8)
# Session 2: Line Items per-filter (8)
# Session 3: Story validation Orders + Line Items (6)
# Email: one report merging all sessions
#
# Example:
#   .\scripts\run-bsd-30019.ps1 -Email "you@paramount.com"

param(
    [Parameter(Mandatory = $true)]
    [string] $Email,

    [ValidateSet("PROD", "UAT", "DEV")]
    [string] $Environment = "UAT"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$aggregateDir = Join-Path $repoRoot "test-output\bsd-30019-aggregate"
$insprint = "src/test/resources/insprint-automation"

$sessions = @(
    @{ Name = "session1-orders-perfilter"; Suite = "$insprint/BSD-30019_OrdersPerFilter_ProdServerSuite.xml" },
    @{ Name = "session2-lineitems-perfilter"; Suite = "$insprint/BSD-30019_LineItemsPerFilter_ProdServerSuite.xml" },
    @{ Name = "session3-story-validation"; Suite = "$insprint/BSD-30019_StoryValidation_ProdServerSuite.xml" }
)

if (Test-Path $aggregateDir) { Remove-Item -Recurse -Force $aggregateDir }
New-Item -ItemType Directory -Force -Path $aggregateDir | Out-Null

foreach ($session in $sessions) {
    Write-Host ""
    Write-Host "=== $($session.Name) ==="
    mvn -B test `
        "-DsuiteXmlFile=$($session.Suite)" `
        "-Dsystem.test.testenvironment=$Environment" `
        "-Dsystem.test.sendreportautoemails=false"

    $resultsFile = Join-Path $repoRoot "target\surefire-reports\testng-results.xml"
    if (-not (Test-Path $resultsFile)) {
        Write-Warning "Missing testng-results.xml after $($session.Name)"
        continue
    }
    Copy-Item $resultsFile (Join-Path $aggregateDir "$($session.Name)-testng-results.xml") -Force
}

Write-Host ""
Write-Host "=== Sending ONE combined email ==="
mvn -B test `
    "-DsuiteXmlFile=src/test/resources/insprint-automation/BSD-30019_SendCombinedEmail_ProdServerSuite.xml" `
    "-Dsystem.test.testenvironment=$Environment" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.aggregateresultsdir=test-output/bsd-30019-aggregate"

Write-Host "Done. Expect ONE email with 22 scenarios (8 + 8 + 6)."
