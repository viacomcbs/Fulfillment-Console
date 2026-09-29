# Creates Desktop + Start Menu shortcuts for the QA Agent Hub launcher.
# Also registers global hotkey listener (default Ctrl+Shift+H).
#
#   .\scripts\qa-agent-hub\Install-QAAgentHubShortcut.ps1

param(
    [string] $Hotkey
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$launcherCmd = Join-Path $repoRoot "scripts\QA Agent Hub.cmd"

if (-not (Test-Path $launcherCmd)) {
    throw "Launcher not found: $launcherCmd"
}

$iconIco = Join-Path $PSScriptRoot "assets\qa-agent-hub-icon.ico"
$iconForShortcut = "$env:SystemRoot\System32\imageres.dll,109"
if (Test-Path $iconIco) {
    $iconForShortcut = $iconIco
}

$wsh = New-Object -ComObject WScript.Shell

$desktopLink = Join-Path ([Environment]::GetFolderPath("Desktop")) "QA Agent Hub.lnk"
$startMenuDir = Join-Path ([Environment]::GetFolderPath("StartMenu")) "Programs"
New-Item -ItemType Directory -Force -Path $startMenuDir | Out-Null
$startMenuLink = Join-Path $startMenuDir "QA Agent Hub.lnk"

function New-HubShortcut {
    param([string] $LinkPath)
    $sc = $wsh.CreateShortcut($LinkPath)
    $sc.TargetPath = $launcherCmd
    $sc.WorkingDirectory = $repoRoot
    $sc.WindowStyle = 1
    $sc.Description = "Media Platform QA Cursor agents (Airtable, WSR, release labels, defects)"
    $sc.IconLocation = $iconForShortcut
    $sc.Save()
}

New-HubShortcut -LinkPath $desktopLink
New-HubShortcut -LinkPath $startMenuLink

Write-Host "Shortcuts created:" -ForegroundColor Green
Write-Host "  Desktop    : $desktopLink"
Write-Host "  Start Menu : $startMenuLink"
