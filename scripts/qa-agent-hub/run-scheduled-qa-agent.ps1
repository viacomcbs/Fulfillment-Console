# Runs a QA Agent Hub job: opens Cursor chat with the agent prompt on clipboard.
# Scheduled via Register-QAAgentSchedule.ps1 (Windows Task Scheduler).
#
#   .\scripts\qa-agent-hub\run-scheduled-qa-agent.ps1 -ConfigPath .\scripts\qa-agent-hub\jobs\weekly-wsr.json

param(
    [Parameter(Mandatory = $true)]
    [string] $ConfigPath
)

$ErrorActionPreference = "Stop"
$hubRoot = $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $hubRoot "..\..")).Path
. (Join-Path $hubRoot "HubSchedule.ps1")

$configFull = (Resolve-Path $ConfigPath).Path
$config = Get-Content $configFull -Raw -Encoding UTF8 | ConvertFrom-Json

$jobName = [string]$config.jobName
$prompt = [string]$config.prompt
$workspaceKey = [string]$config.workspace
$catalogPath = Join-Path $hubRoot "agents-catalog.json"
$catalog = Get-Content $catalogPath -Raw -Encoding UTF8 | ConvertFrom-Json

$workspace = [string]$catalog.defaultWorkspace
if ($catalog.workspaces.$workspaceKey) {
    $workspace = [string]$catalog.workspaces.$workspaceKey
}

$logDir = Join-Path $repoRoot "test-output\qa-agent-hub-logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$logFile = Join-Path $logDir ("{0}_{1}.log" -f ($jobName -replace '[^\w\-]', '-'), (Get-Date -Format "yyyyMMdd_HHmmss"))

function Write-HubLog([string] $Message) {
    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') $Message"
    Add-Content -Path $logFile -Value $line -Encoding UTF8
    Write-Host $line
}

Write-HubLog "Job=$jobName Agent=$($config.agentTitle)"
Write-HubLog "Workspace=$workspace"
Write-HubLog "Prompt length=$($prompt.Length) chars"
if ($config.notifications) {
    Write-HubLog "Notify Slack=$($config.notifications.slackChannel) Email=$($config.notifications.reportEmail)"
}

$cursor = Get-Command cursor -ErrorAction SilentlyContinue
if (-not $cursor) {
    $fallback = Join-Path $env:LOCALAPPDATA "Programs\cursor\resources\app\bin\cursor.cmd"
    if (Test-Path $fallback) { $cursor = Get-Item $fallback }
}
if (-not $cursor) {
    Write-HubLog "ERROR: Cursor CLI not found."
    exit 1
}

Set-Clipboard -Value $prompt
Write-HubLog "Prompt copied to clipboard."

if (Test-Path $workspace) {
    Start-Process -FilePath $cursor.Source -ArgumentList @("-n", $workspace, "--chat")
    Write-HubLog "Opened Cursor chat with workspace."
} else {
    Start-Process -FilePath $cursor.Source -ArgumentList @("--chat")
    Write-HubLog "Opened Cursor chat (workspace missing)."
}

# Toast notification (Windows 10+)
try {
    Add-Type -AssemblyName System.Windows.Forms
    [System.Windows.Forms.MessageBox]::Show(
        "Scheduled QA agent: $($config.agentTitle)`n`nCursor chat opened. Prompt is on clipboard - press Ctrl+V.`n`nLog: $logFile",
        "QA Agent Hub",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Information) | Out-Null
} catch {
    Write-HubLog "Could not show notification dialog."
}

Write-HubLog "Done."
exit 0
