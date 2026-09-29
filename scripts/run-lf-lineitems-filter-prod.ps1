# Line Items left filters — run ONE filter on PROD (one Synergy session, email to you only).
#
# Examples:
#   .\scripts\run-lf-lineitems-filter-prod.ps1 -Filter flag
#   .\scripts\run-lf-lineitems-filter-prod.ps1 -Filter lineitemstatus -SkipCompile
#   .\scripts\run-lf-lineitems-filter-prod.ps1 -ListFilters
#
# Regenerate suite XMLs after adding tests:
#   python scripts/build_lf_li_split_suites.py

param(
    [string] $Filter = "",

    [string] $Email = "Akilandeswari.Sundararajan@paramount.com",

    [switch] $SkipCompile,
    [switch] $ListFilters
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$sessionsDir = Join-Path $repoRoot "src\test\resources\regression\left-filters\line-items-view\sessions"
$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

$available = @(Get-ChildItem $sessionsDir -Filter "LF_LI_Split_*_ProdServerSuite.xml" -ErrorAction SilentlyContinue |
    ForEach-Object {
        if ($_.Name -match '^LF_LI_Split_(.+)_ProdServerSuite\.xml$') {
            [PSCustomObject]@{
                Key   = $Matches[1]
                Suite = "src/test/resources/regression/left-filters/line-items-view/sessions/$($_.Name)"
            }
        }
    } | Sort-Object Key)

if ($ListFilters -or [string]::IsNullOrWhiteSpace($Filter)) {
    Write-Host "=== Line Items left-filter PROD suites ===" -ForegroundColor Green
    Write-Host "Environment : PROD"
    Write-Host "Email       : $Email"
    Write-Host ""
    if ($available.Count -eq 0) {
        Write-Host "No suite XMLs found. Run: python scripts/build_lf_li_split_suites.py" -ForegroundColor Yellow
        exit 1
    }
    foreach ($item in $available) {
        Write-Host "  $($item.Key) -> $($item.Suite)"
    }
    Write-Host ""
    Write-Host "Run: .\scripts\run-lf-lineitems-filter-prod.ps1 -Filter <key>" -ForegroundColor Cyan
    if ([string]::IsNullOrWhiteSpace($Filter)) { exit 0 }
}

$filterKey = $Filter.Trim().ToLower()
$match = $available | Where-Object { $_.Key -eq $filterKey } | Select-Object -First 1
if (-not $match) {
    throw "Unknown filter '$Filter'. Use -ListFilters to see keys (e.g. flag, lineitemstatus, assignedto)."
}

$suiteRel = $match.Suite
$filterLabel = ($filterKey -replace '([a-z])([A-Z])', '$1 $2') -replace '_', ' '
$reportTitle = "Line Items Tab - Left Filters (PROD) - $filterLabel"

Write-Host "=== Line Items Left Filters - $filterLabel ===" -ForegroundColor Green
Write-Host "Filter      : $filterKey"
Write-Host "Environment : PROD"
Write-Host "Email       : $Email"
Write-Host "Suite       : $suiteRel"
Write-Host ""

if (-not $SkipCompile) {
    Write-Host "=== Compiling tests ===" -ForegroundColor Cyan
    mvn -B test-compile
    if ($LASTEXITCODE -ne 0) { throw "test-compile failed" }
}

function Test-SynergyServerReachable {
    $url = "https://www.synergyserver.tech"
    try {
        Invoke-WebRequest -Uri $url -TimeoutSec 15 -UseBasicParsing | Out-Null
        Write-Host "Synergy server reachable: $url" -ForegroundColor Green
        return $true
    } catch {
        Write-Warning "Synergy server not reachable: $url"
        return $false
    }
}

Write-Host "=== Pre-flight: Synergy server check ===" -ForegroundColor Cyan
if (-not (Test-SynergyServerReachable)) {
    throw "Synergy server is not reachable. Check network/VPN, then re-run."
}
Write-Host ""

Write-Host "=== Running $filterLabel ===" -ForegroundColor Cyan
mvn -B test `
    "-DsuiteXmlFile=$suiteRel" `
    "-Dsystem.test.testenvironment=PROD" `
    "-Dsystem.test.sendreportautoemails=true" `
    "-Dsystem.test.sendreportemailaddress=$Email" `
    "-Dsystem.test.sendchatreport=false" `
    "-Dsystem.test.leftfilteremailsuitetitle=$reportTitle" `
    "-Dsystem.test.userkey=$akilaUserKey" `
    "-Dsystem.test.clientid=$akilaClientId"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Run finished with failures (exit $LASTEXITCODE)" -ForegroundColor Yellow
} else {
    Write-Host "Run finished successfully." -ForegroundColor Green
}

Write-Host ""
Write-Host "Done - Line Items $filterLabel PROD run complete." -ForegroundColor Green
