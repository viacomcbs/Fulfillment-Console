# Shared helpers for QA Agent Hub job configs and Windows Task Scheduler.

function Get-HubSettings {
    param([string] $HubRoot)
    $path = Join-Path $HubRoot "hub-settings.json"
    if (-not (Test-Path $path)) { return $null }
    return (Get-Content $path -Raw -Encoding UTF8 | ConvertFrom-Json)
}

function Get-HubAgentById {
    param(
        [string] $AgentId,
        $Catalog
    )
    foreach ($a in @($Catalog.agents)) {
        if ([string]$a.id -eq $AgentId) { return $a }
    }
    return $null
}

function Get-HubAgentPrompt {
    param(
        [string] $AgentId,
        $Catalog,
        $Settings
    )
    $agent = Get-HubAgentById -AgentId $AgentId -Catalog $Catalog
    if (-not $agent) { throw "Unknown agent id: $AgentId" }

    if ([string]$agent.type -eq "submenu") {
        throw "Release label agent requires a product choice. Schedule a specific option from the hub UI."
    }

    $prompt = [string]$agent.prompt
    if ($AgentId -eq "update-wsr" -and $Settings -and $Settings.notifications.wsr) {
        $n = $Settings.notifications.wsr
        $slack = [string]$n.slackChannel
        $email = [string]$n.reportEmail
        $slackEmail = [string]$n.slackEmail
        $prompt += " After preparing the WSR copy-paste content, send the complete weekly summary to Slack channel $slack using Slack MCP (slack_send_message). Also send the same summary to $email and to Slack via email $slackEmail."
    }
    return $prompt
}

function Save-HubJobConfig {
    param(
        [string] $HubRoot,
        [string] $JobName,
        [string] $AgentId,
        $Catalog,
        $Settings,
        [string] $Frequency,
        [string] $Time,
        [string[]] $DaysOfWeek,
        [string] $StartDate
    )

    $agent = Get-HubAgentById -AgentId $AgentId -Catalog $Catalog
    if (-not $agent) { throw "Unknown agent: $AgentId" }

    $jobsDir = Join-Path $HubRoot "jobs"
    New-Item -ItemType Directory -Force -Path $jobsDir | Out-Null

    $config = [ordered]@{
        jobName    = $JobName
        agentId    = $AgentId
        agentTitle = [string]$agent.title
        prompt     = (Get-HubAgentPrompt -AgentId $AgentId -Catalog $Catalog -Settings $Settings)
        workspace  = [string]$agent.workspace
        schedule   = [ordered]@{
            frequency  = $Frequency
            time       = $Time
            timezone   = "India Standard Time"
            daysOfWeek = @($DaysOfWeek)
            startDate  = $StartDate
        }
    }

    if ($AgentId -eq "update-wsr" -and $Settings -and $Settings.notifications.wsr) {
        $config.notifications = [ordered]@{
            slackChannel = [string]$Settings.notifications.wsr.slackChannel
            reportEmail  = [string]$Settings.notifications.wsr.reportEmail
            slackEmail   = [string]$Settings.notifications.wsr.slackEmail
        }
    }

    $safeName = ($JobName -replace '[^\w\-]', '-').Trim('-')
    if (-not $safeName) { $safeName = "job-$(Get-Date -Format 'yyyyMMdd-HHmmss')" }
    $path = Join-Path $jobsDir "$safeName.json"
    ($config | ConvertTo-Json -Depth 6) | Set-Content -Path $path -Encoding UTF8
    return $path
}

function Get-HubWindowsTaskName {
    param([string] $JobName)
    return "QAAgentHub-$JobName"
}

function Get-HubScheduledJobStatuses {
    param([string] $HubRoot)

    $jobsDir = Join-Path $HubRoot "jobs"
    $rows = New-Object System.Collections.Generic.List[object]
    if (-not (Test-Path $jobsDir)) { return @() }

    foreach ($file in @(Get-ChildItem $jobsDir -Filter "*.json" | Sort-Object Name)) {
        try {
            $cfg = Get-Content $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
        } catch { continue }

        $jobName = [string]$cfg.jobName
        if (-not $jobName) { $jobName = $file.BaseName }
        $freq = if ($cfg.schedule.frequency) { [string]$cfg.schedule.frequency } else { "-" }
        $time = if ($cfg.schedule.time) { [string]$cfg.schedule.time } else { "-" }
        $days = if ($cfg.schedule.daysOfWeek) { (@($cfg.schedule.daysOfWeek) -join ", ") } else { "" }
        $scheduleText = switch ($freq) {
            "weekly" { "weekly ($days) at $time IST" }
            "daily"  { "daily at $time IST" }
            "once"   { "once $($cfg.schedule.startDate) at $time IST" }
            default  { "$freq at $time IST" }
        }

        $taskName = Get-HubWindowsTaskName -JobName $jobName
        $taskState = "Not registered"
        $lastRun = "-"
        $nextRun = "-"
        $lastResult = "-"

        try {
            $task = Get-ScheduledTask -TaskName $taskName -ErrorAction Stop
            $info = $task | Get-ScheduledTaskInfo
            $taskState = [string]$task.State
            if ($info.LastRunTime -and $info.LastRunTime.Year -gt 2000) {
                $lastRun = $info.LastRunTime.ToString("yyyy-MM-dd HH:mm:ss")
            }
            if ($info.NextRunTime -and $info.NextRunTime.Year -gt 2000) {
                $nextRun = $info.NextRunTime.ToString("yyyy-MM-dd HH:mm:ss")
            }
            if ($info.LastTaskResult -eq 0) { $lastResult = "Success (0)" }
            else { $lastResult = "Exit $($info.LastTaskResult)" }
        } catch {
            $taskState = "Not registered"
        }

        $rows.Add([pscustomobject]@{
            JobName    = $jobName
            Agent      = [string]$cfg.agentTitle
            Schedule   = $scheduleText
            TaskName   = $taskName
            TaskState  = $taskState
            LastRun    = $lastRun
            NextRun    = $nextRun
            LastResult = $lastResult
            ConfigPath = $file.FullName
        }) | Out-Null
    }
    return @($rows)
}
