# Combined PROD run: left-filter smoke + Babloo table-view column batch, then dual LF emails.
#
# Phase 1 — LF report smoke (7 Activity Type tests, no email)
# Phase 2 — Table-view column smoke TC_011–TC_020 (email + PDF on completion)
# Phase 3 — Optional LF dual email (stakeholder passed-only + internal full status)
#
# Examples:
#   .\scripts\run-lf-and-tableview-combined-prod.ps1
#   .\scripts\run-lf-and-tableview-combined-prod.ps1 -SkipLfDualEmail
#   .\scripts\run-lf-and-tableview-combined-prod.ps1 -SkipTableView

param(
    [switch] $SkipLfSmoke,
    [switch] $SkipTableView,
    [switch] $SkipLfDualEmail,

    [string] $LfSmokeSuite = "src/test/resources/regression/left-filters/orders-view/LF_O_ReportSmoke_ActivityType_ProdServerSuite.xml",
    [string] $TableViewSuite = "src/test/resources/regression/table-view/ColumnsSuite011-020Config.xml",

    [string] $StakeholderEmail = (
        "Akilandeswari.Sundararajan@paramount.com," +
        "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
    ),
    [string] $InternalEmail = "Akilandeswari.Sundararajan@paramount.com",
    [string] $TableViewEmail = "Akilandeswari.Sundararajan@paramount.com",

    [string] $SlackChannel = "#qa_automation_status_go"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

# Akila PROD Synergy credentials (override Babloo defaults baked into table-view suite XMLs)
$akilaUserKey = "af27f06e-2e7e-4c8d-9312-2320423e4641"
$akilaClientId = "5CG23257SH"

Write-Host "=== Compiling tests ==="
mvn -B test-compile

if (-not $SkipLfSmoke) {
    Write-Host ""
    Write-Host "=== Phase 1: LF report smoke (no email) ==="
    Write-Host "Suite: $LfSmokeSuite"
    mvn -B test `
        "-DsuiteXmlFile=$LfSmokeSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=false" `
        "-Dsystem.test.sendchatreport=false" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"
}

if (-not $SkipTableView) {
    Write-Host ""
    Write-Host "=== Phase 2: Table-view column smoke TC_011–TC_020 ==="
    Write-Host "Suite: $TableViewSuite"
    Write-Host "Email: $TableViewEmail"
    mvn -B test `
        "-DsuiteXmlFile=$TableViewSuite" `
        "-Dsystem.test.testenvironment=PROD" `
        "-Dsystem.test.sendreportautoemails=true" `
        "-Dsystem.test.sendreportemailaddress=$TableViewEmail" `
        "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
        "-Dsystem.test.uploadpdf=true" `
        "-Dsystem.test.attachpdfreport=true" `
        "-Dsystem.test.userkey=$akilaUserKey" `
        "-Dsystem.test.clientid=$akilaClientId"
}

if (-not $SkipLfDualEmail -and -not $SkipLfSmoke) {
    Write-Host ""
    Write-Host "=== Phase 3: LF dual email (stakeholder passed-only + internal full status) ==="
    $aggregateDir = Join-Path $repoRoot "test-output\lf-o-passed-aggregate"
    $combinedSuite = "src/test/resources/regression/left-filters/orders-view/sessions/LF_O_Passed_CombinedEmail_ProdServerSuite.xml"

    if (-not (Test-Path $aggregateDir)) {
        Write-Warning @"
Aggregate dir not found: $aggregateDir
Skipping dual LF email. Run .\scripts\run-orders-left-filter-passed-prod.ps1 first for full LF regression emails,
or use -SkipLfDualEmail when running smoke-only.
"@
    } else {
        if (-not $env:SLACK_WEBHOOK_URL) {
            Write-Warning "SLACK_WEBHOOK_URL is not set — Slack API post will be skipped."
        }
        $slackArgs = @()
        if ($env:SLACK_WEBHOOK_URL) {
            $slackArgs += "-Dsystem.test.slackwebhookurl=$($env:SLACK_WEBHOOK_URL)"
        }
        mvn -B test `
            "-DsuiteXmlFile=$combinedSuite" `
            "-Dsystem.test.testenvironment=PROD" `
            "-Dsystem.test.sendreportemailaddress=$StakeholderEmail" `
            "-Dsystem.test.sendreportinternalemailaddress=$InternalEmail" `
            "-Dsystem.test.sendchatreport=true" `
            "-Dsystem.test.slackchannel=$SlackChannel" `
            @slackArgs
    }
}

Write-Host ""
Write-Host "Done."
Write-Host "  LF smoke           : $(if ($SkipLfSmoke) { 'skipped' } else { 'ran' })"
Write-Host "  Table-view smoke   : $(if ($SkipTableView) { 'skipped' } else { 'ran' })"
Write-Host "  LF dual email      : $(if ($SkipLfDualEmail -or $SkipLfSmoke) { 'skipped' } else { 'attempted' })"
Write-Host "  Table-view email   : $TableViewEmail"
