# One-time helper: save + register weekly WSR schedule (Thu 9 AM IST).
$ErrorActionPreference = "Stop"
$hubRoot = $PSScriptRoot
. (Join-Path $hubRoot "HubSchedule.ps1")

$catalog = Get-Content (Join-Path $hubRoot "agents-catalog.json") -Raw -Encoding UTF8 | ConvertFrom-Json
$settings = Get-HubSettings -HubRoot $hubRoot

$cfg = Save-HubJobConfig -HubRoot $hubRoot -JobName "weekly-wsr-update" -AgentId "update-wsr" `
    -Catalog $catalog -Settings $settings -Frequency "weekly" -Time "09:00" `
    -DaysOfWeek @("Thursday") -StartDate (Get-Date -Format "yyyy-MM-dd")

Write-Host "Saved: $cfg" -ForegroundColor Cyan
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $hubRoot "Register-QAAgentSchedule.ps1") -ConfigPath $cfg
