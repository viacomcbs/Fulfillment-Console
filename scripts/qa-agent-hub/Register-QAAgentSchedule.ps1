# Registers or removes a Windows Scheduled Task for a QA Agent Hub job JSON.
# Timezone: India Standard Time (Bangalore).
#
#   .\scripts\qa-agent-hub\Register-QAAgentSchedule.ps1 -ConfigPath .\scripts\qa-agent-hub\jobs\weekly-wsr.json
#   .\scripts\qa-agent-hub\Register-QAAgentSchedule.ps1 -ConfigPath .\jobs\weekly-wsr.json -Remove

param(
    [Parameter(Mandatory = $true)]
    [string] $ConfigPath,

    [switch] $Remove,

    [switch] $RunWhenLoggedOff,

    [switch] $WakeComputer
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "HubSchedule.ps1")

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$configFull = (Resolve-Path $ConfigPath).Path
$config = Get-Content $configFull -Raw -Encoding UTF8 | ConvertFrom-Json

if (-not $config.jobName) { throw "jobName is required in config JSON." }
if (-not $config.schedule) { throw "schedule block is required in config JSON." }

$taskName = Get-HubWindowsTaskName -JobName ([string]$config.jobName)
$runner = Join-Path $PSScriptRoot "run-scheduled-qa-agent.ps1"
$arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$runner`" -ConfigPath `"$configFull`""

if ($Remove) {
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
    Write-Host "Removed scheduled task: $taskName" -ForegroundColor Yellow
    exit 0
}

$frequency = [string]$config.schedule.frequency
$timeText = if ($config.schedule.time) { [string]$config.schedule.time } else { "09:00" }
if ($timeText -notmatch '^\d{1,2}:\d{2}$') {
    throw "schedule.time must be HH:mm (24h), e.g. 09:00 for 9 AM IST."
}

$hour, $minute = $timeText.Split(":")
$atTime = [datetime]::Today.AddHours([int]$hour).AddMinutes([int]$minute)

switch ($frequency) {
    "once" {
        if (-not $config.schedule.startDate) {
            throw "schedule.startDate (yyyy-MM-dd) is required for frequency=once."
        }
        $start = [datetime]::ParseExact([string]$config.schedule.startDate, "yyyy-MM-dd", $null)
        $at = $start.Date.AddHours([int]$hour).AddMinutes([int]$minute)
        if ($at -lt (Get-Date)) { throw "Once schedule is in the past: $at" }
        $trigger = New-ScheduledTaskTrigger -Once -At $at
    }
    "daily" {
        $trigger = New-ScheduledTaskTrigger -Daily -At $atTime
    }
    "weekly" {
        $days = @($config.schedule.daysOfWeek)
        if ($days.Count -eq 0) { $days = @("Thursday") }
        $dow = $days | ForEach-Object {
            [System.Enum]::Parse([System.DayOfWeek], [string]$_, $true)
        }
        $trigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek $dow -At $atTime
    }
    default {
        throw "Unsupported schedule.frequency: $frequency (use once, daily, weekly)."
    }
}

$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument $arguments -WorkingDirectory $repoRoot
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -ExecutionTimeLimit (New-TimeSpan -Hours 4)

if ($WakeComputer) { $settings.WakeToRun = $true }

$description = "QA Agent Hub: $($config.agentTitle) ($($config.jobName)) - IST schedule."

if ($RunWhenLoggedOff) {
    $cred = Get-Credential -Message "Windows password required to run QA agents when you are not logged in."
    $principal = New-ScheduledTaskPrincipal -UserId $cred.UserName -LogonType Password -RunLevel Highest
    Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger `
        -Settings $settings -Principal $principal -Description $description -Force | Out-Null
} else {
    $principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType Interactive -RunLevel Highest
    Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger `
        -Settings $settings -Principal $principal -Description $description -Force | Out-Null
}

Write-Host "Registered scheduled task: $taskName" -ForegroundColor Green
Write-Host "  Agent     : $($config.agentTitle)"
Write-Host "  Frequency : $frequency at $timeText IST"
Write-Host "  Config    : $configFull"
Write-Host ""
Write-Host "Verify: Get-ScheduledTask -TaskName '$taskName' | Get-ScheduledTaskInfo"
