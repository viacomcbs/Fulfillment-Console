# Installs QA Agent Hub shortcuts + global hotkey listener at logon.
# Default hotkey: Ctrl+Shift+H
#
#   .\scripts\qa-agent-hub\Register-QAAgentHubHotkey.ps1

param(
    [switch] $UseCtrl = $true,
    [switch] $UseAlt,
    [switch] $UseShift = $true,
    [string] $Key = "H",
    [switch] $Remove
)

$ErrorActionPreference = "Stop"
$hubRoot = $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $hubRoot "..\..")).Path
$listener = Join-Path $hubRoot "QAAgentHubHotkeyListener.ps1"
$installShortcuts = Join-Path $hubRoot "Install-QAAgentHubShortcut.ps1"
$taskName = "QAAgentHub-Hotkey"
$settingsPath = Join-Path $hubRoot "hub-hotkey.json"

function Get-HotkeyLabel {
    param(
        [bool] $Ctrl,
        [bool] $Alt,
        [bool] $Shift,
        [string] $KeyName
    )
    $parts = @()
    if ($Ctrl) { $parts += "Ctrl" }
    if ($Alt) { $parts += "Alt" }
    if ($Shift) { $parts += "Shift" }
    $parts += $KeyName
    return ($parts -join "+")
}

if ($Remove) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue | Out-Null
    if (Test-Path $settingsPath) { Remove-Item $settingsPath -Force }
    Write-Host "Removed QA Agent Hub hotkey listener task." -ForegroundColor Yellow
    exit 0
}

if (-not (Test-Path $listener)) {
    throw "Missing listener script: $listener"
}

& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $installShortcuts

$hotkeyLabel = Get-HotkeyLabel -Ctrl:$UseCtrl -Alt:$UseAlt -Shift:$UseShift -KeyName $Key
$settings = [ordered]@{
    hotkey = $hotkeyLabel
    useCtrl = [bool]$UseCtrl
    useAlt = [bool]$UseAlt
    useShift = [bool]$UseShift
    key = $Key
    installedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
}
($settings | ConvertTo-Json) | Set-Content -Path $settingsPath -Encoding UTF8

$argList = @(
    "-NoProfile",
    "-ExecutionPolicy", "Bypass",
    "-WindowStyle", "Hidden",
    "-Sta",
    "-File", $listener
)
if ($UseCtrl) { $argList += "-UseCtrl" }
if ($UseAlt) { $argList += "-UseAlt" }
if ($UseShift) { $argList += "-UseShift" }
if ($Key -and $Key -ne "H") { $argList += "-Key"; $argList += $Key }

$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument ($argList -join " ")
$trigger = New-ScheduledTaskTrigger -AtLogOn -User $env:USERNAME
$settingsTask = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable
$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive -RunLevel Limited

Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue | Out-Null
Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Settings $settingsTask -Principal $principal -Description "Global hotkey listener for Media Platform QA Agent Hub ($hotkeyLabel)" | Out-Null

Start-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue

Write-Host "QA Agent Hub hotkey installed." -ForegroundColor Green
Write-Host "  Hotkey : $hotkeyLabel" -ForegroundColor Cyan
Write-Host "  Task   : $taskName (starts at logon)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press $hotkeyLabel now to open QA Agent Hub."
