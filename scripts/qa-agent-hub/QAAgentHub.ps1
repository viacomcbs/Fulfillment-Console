# WinForms launcher for Media Platform QA Cursor agents (Airtable, WSR, release labels, defects).
# Launch: .\scripts\Launch-QAAgentHub.ps1

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"
$hubRoot = $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $hubRoot "..\..")).Path
$catalogPath = Join-Path $hubRoot "agents-catalog.json"

if (-not (Test-Path $catalogPath)) {
    throw "Missing agents catalog: $catalogPath"
}

$catalog = Get-Content $catalogPath -Raw -Encoding UTF8 | ConvertFrom-Json
. (Join-Path $hubRoot "HubSchedule.ps1")
$hubSettings = Get-HubSettings -HubRoot $hubRoot
$jobsDir = Join-Path $hubRoot "jobs"
New-Item -ItemType Directory -Force -Path $jobsDir | Out-Null

$ThemeRoyalBlue = [System.Drawing.Color]::FromArgb(28, 58, 138)
$ThemeRoyalBlueLight = [System.Drawing.Color]::FromArgb(42, 78, 168)
$ThemeTextLight = [System.Drawing.Color]::White
$ThemeTextSoft = [System.Drawing.Color]::FromArgb(230, 240, 255)
$ThemeInputBack = [System.Drawing.Color]::White
$ThemeBtnGreen = [System.Drawing.Color]::FromArgb(76, 175, 80)
$ThemeBtnYellow = [System.Drawing.Color]::FromArgb(255, 193, 7)
$ThemeBtnPink = [System.Drawing.Color]::FromArgb(236, 64, 122)
$ThemeBtnBlue = [System.Drawing.Color]::FromArgb(33, 150, 243)
$ThemeBtnText = [System.Drawing.Color]::FromArgb(25, 25, 35)
$ThemeFont = New-Object System.Drawing.Font("Segoe UI", 9)
$ThemeFontBold = New-Object System.Drawing.Font("Segoe UI", 9, [System.Drawing.FontStyle]::Bold)
$ThemeFontTitle = New-Object System.Drawing.Font("Segoe UI", 11, [System.Drawing.FontStyle]::Bold)
$ThemeFontTile = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)

function Convert-HexToColor {
    param([string] $Hex)
    if (-not $Hex) { return $ThemeBtnBlue }
    $h = $Hex.TrimStart("#")
    if ($h.Length -ne 6) { return $ThemeBtnBlue }
    return [System.Drawing.Color]::FromArgb(
        [Convert]::ToInt32($h.Substring(0, 2), 16),
        [Convert]::ToInt32($h.Substring(2, 2), 16),
        [Convert]::ToInt32($h.Substring(4, 2), 16))
}

function Set-ActionButtonTheme {
    param(
        [System.Windows.Forms.Button] $Button,
        [System.Drawing.Color] $BackColor,
        [System.Drawing.Color] $ForeColor = $ThemeBtnText
    )
    $Button.FlatStyle = [System.Windows.Forms.FlatStyle]::Flat
    $Button.BackColor = $BackColor
    $Button.ForeColor = $ForeColor
    $Button.Font = $ThemeFontBold
    $Button.FlatAppearance.BorderSize = 0
    $Button.Cursor = [System.Windows.Forms.Cursors]::Hand
    $hover = [System.Drawing.Color]::FromArgb(
        [Math]::Min(255, $BackColor.R + 20),
        [Math]::Min(255, $BackColor.G + 20),
        [Math]::Min(255, $BackColor.B + 20))
    $Button.FlatAppearance.MouseOverBackColor = $hover
    $Button.FlatAppearance.MouseDownBackColor = $BackColor
}

function Get-CursorExecutable {
    $cmd = Get-Command cursor -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $fallback = Join-Path $env:LOCALAPPDATA "Programs\cursor\resources\app\bin\cursor.cmd"
    if (Test-Path $fallback) { return $fallback }
    throw "Cursor CLI not found. Install Cursor or add cursor to PATH."
}

function Resolve-HubWorkspace {
    param([string] $Key)
    if (-not $Key) {
        return [string]$catalog.defaultWorkspace
    }
    if ($catalog.workspaces.$Key) {
        return [string]$catalog.workspaces.$Key
    }
    if (Test-Path $Key) { return $Key }
    return [string]$catalog.defaultWorkspace
}

function Resolve-HubRelativePath {
    param([string] $RelativePath)
    if (-not $RelativePath) { return $null }
    $combined = Join-Path $repoRoot $RelativePath
    return [System.IO.Path]::GetFullPath($combined)
}

function Start-HubExternalApp {
    param(
        [string] $AppTitle,
        [string] $ScriptPath,
        [string] $LauncherPath = ""
    )

    $target = $ScriptPath
    if (-not $target -or -not (Test-Path $target)) {
        $target = $LauncherPath
    }
    if (-not $target -or -not (Test-Path $target)) {
        throw "App not found: $AppTitle`nExpected: $ScriptPath"
    }

    $workDir = Split-Path $target -Parent
    Start-Process -FilePath "powershell.exe" -WorkingDirectory $workDir -ArgumentList @(
        "-NoProfile", "-ExecutionPolicy", "Bypass", "-Sta", "-File", $target
    ) | Out-Null
    Append-Log "Opened $AppTitle."
}

function Invoke-QAAgentPrompt {
    param(
        [string] $AgentTitle,
        [string] $Prompt,
        [string] $WorkspaceKey,
        [string] $AgentId = ""
    )

    if ($AgentId) {
        $Prompt = Get-HubAgentPrompt -AgentId $AgentId -Catalog $catalog -Settings $hubSettings
    }
    if (-not $Prompt) {
        throw "No prompt configured for this agent."
    }

    $workspace = Resolve-HubWorkspace -Key $WorkspaceKey
    [System.Windows.Forms.Clipboard]::SetText($Prompt)

    $cursor = Get-CursorExecutable
    if (Test-Path $workspace) {
        Start-Process -FilePath $cursor -ArgumentList @("-n", $workspace, "--chat")
    } else {
        Start-Process -FilePath $cursor -ArgumentList @("--chat")
        Append-Log "WARNING: Workspace not found ($workspace). Opened Cursor chat without folder."
    }

    Append-Log "[$AgentTitle] Prompt copied. Cursor chat opened."
    Append-Log "Paste in chat with Ctrl+V if needed."

    [System.Windows.Forms.MessageBox]::Show(
        "Cursor chat is opening.`n`nThe agent prompt is on your clipboard - press Ctrl+V in chat:`n`n$Prompt",
        $AgentTitle,
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Information) | Out-Null
}

function Show-ReleaseLabelPicker {
    param($Agent)

    $dlg = New-Object System.Windows.Forms.Form
    $dlg.Text = "Update UAT Release Label"
    $dlg.Size = New-Object System.Drawing.Size(420, 280)
    $dlg.StartPosition = "CenterParent"
    $dlg.BackColor = $ThemeRoyalBlue
    $dlg.Font = $ThemeFont

    $lbl = New-Object System.Windows.Forms.Label
    $lbl.Text = "Choose product (latest UAT release from Confluence):"
    $lbl.Location = New-Object System.Drawing.Point(16, 16)
    $lbl.Size = New-Object System.Drawing.Size(380, 40)
    $lbl.ForeColor = $ThemeTextLight
    $lbl.BackColor = [System.Drawing.Color]::Transparent
    $dlg.Controls.Add($lbl)

    $y = 64
    foreach ($opt in @($Agent.options)) {
        $btn = New-Object System.Windows.Forms.Button
        $btn.Text = [string]$opt.title
        $btn.Location = New-Object System.Drawing.Point(16, $y)
        $btn.Size = New-Object System.Drawing.Size(372, 40)
        Set-ActionButtonTheme -Button $btn -BackColor (Convert-HexToColor $Agent.color)
        $prompt = [string]$opt.prompt
        $ws = [string]$Agent.workspace
        $btn.Add_Click({
            try {
                Invoke-QAAgentPrompt -AgentTitle ([string]$opt.title) -Prompt $prompt -WorkspaceKey $ws
                $dlg.Close()
            } catch {
                Append-Log "ERROR: $($_.Exception.Message)"
                [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Launch failed", "OK", "Error") | Out-Null
            }
        }.GetNewClosure())
        $dlg.Controls.Add($btn)
        $y += 48
    }

    [void]$dlg.ShowDialog($form)
}

function Show-HubScheduledJobsDialog {
    $dlg = New-Object System.Windows.Forms.Form
    $dlg.Text = "Scheduled QA agents"
    $dlg.Size = New-Object System.Drawing.Size(900, 380)
    $dlg.StartPosition = "CenterParent"
    $dlg.BackColor = $ThemeRoyalBlue
    $dlg.Font = $ThemeFont

    $lv = New-Object System.Windows.Forms.ListView
    $lv.Location = New-Object System.Drawing.Point(12, 12)
    $lv.Size = New-Object System.Drawing.Size(860, 280)
    $lv.View = "Details"
    $lv.FullRowSelect = $true
    $lv.GridLines = $true
    $lv.BackColor = $ThemeInputBack
    [void]$lv.Columns.Add("Job", 120)
    [void]$lv.Columns.Add("Agent", 140)
    [void]$lv.Columns.Add("Schedule (IST)", 180)
    [void]$lv.Columns.Add("Windows task", 170)
    [void]$lv.Columns.Add("State", 80)
    [void]$lv.Columns.Add("Last run", 110)
    [void]$lv.Columns.Add("Next run", 110)
    $dlg.Controls.Add($lv)

    $populate = {
        $lv.Items.Clear()
        foreach ($row in Get-HubScheduledJobStatuses -HubRoot $hubRoot) {
            $item = New-Object System.Windows.Forms.ListViewItem($row.JobName)
            [void]$item.SubItems.Add($row.Agent)
            [void]$item.SubItems.Add($row.Schedule)
            [void]$item.SubItems.Add($row.TaskName)
            [void]$item.SubItems.Add($row.TaskState)
            [void]$item.SubItems.Add($row.LastRun)
            [void]$item.SubItems.Add($row.NextRun)
            if ($row.TaskState -eq "Not registered") {
                $item.BackColor = [System.Drawing.Color]::FromArgb(255, 230, 230)
            }
            [void]$lv.Items.Add($item)
        }
        if ($lv.Items.Count -eq 0) {
            [void]$lv.Items.Add((New-Object System.Windows.Forms.ListViewItem("(no scheduled jobs)")))
        }
    }

    $btnRefresh = New-Object System.Windows.Forms.Button
    $btnRefresh.Text = "Refresh"
    $btnRefresh.Location = New-Object System.Drawing.Point(12, 302)
    $btnRefresh.Size = New-Object System.Drawing.Size(90, 28)
    Set-ActionButtonTheme -Button $btnRefresh -BackColor $ThemeBtnYellow
    $btnRefresh.Add_Click($populate)
    $dlg.Controls.Add($btnRefresh)

    $btnClose = New-Object System.Windows.Forms.Button
    $btnClose.Text = "Close"
    $btnClose.Location = New-Object System.Drawing.Point(782, 302)
    $btnClose.Size = New-Object System.Drawing.Size(90, 28)
    Set-ActionButtonTheme -Button $btnClose -BackColor $ThemeBtnPink
    $btnClose.Add_Click({ $dlg.Close() })
    $dlg.Controls.Add($btnClose)

    & $populate
    [void]$dlg.ShowDialog($form)
}

function Show-ScheduleAgentDialog {
    param([string] $PreselectAgentId = "")

    $dlg = New-Object System.Windows.Forms.Form
    $dlg.Text = "Schedule QA agent"
    $dlg.Size = New-Object System.Drawing.Size(460, 420)
    $dlg.StartPosition = "CenterParent"
    $dlg.BackColor = $ThemeRoyalBlue
    $dlg.Font = $ThemeFont

    $y = 16
    $lblAgent = New-Object System.Windows.Forms.Label
    $lblAgent.Text = "Agent:"
    $lblAgent.Location = New-Object System.Drawing.Point(16, $y)
    $lblAgent.ForeColor = $ThemeTextLight
    $lblAgent.AutoSize = $true
    $dlg.Controls.Add($lblAgent)

    $cmbAgent = New-Object System.Windows.Forms.ComboBox
    $cmbAgent.Location = New-Object System.Drawing.Point(120, ($y - 3))
    $cmbAgent.Size = New-Object System.Drawing.Size(310, 24)
    $cmbAgent.DropDownStyle = "DropDownList"
    $scheduleable = @($catalog.agents | Where-Object {
        [string]$_.type -ne "submenu" -and ($_.scheduleable -ne $false)
    })
    foreach ($a in $scheduleable) {
        [void]$cmbAgent.Items.Add([string]$a.id)
    }
    if ($cmbAgent.Items.Count -eq 0) {
        throw "No scheduleable agents in catalog."
    }
    if ($PreselectAgentId -and ($PreselectAgentId -in @($cmbAgent.Items))) {
        $cmbAgent.SelectedItem = $PreselectAgentId
    } else {
        $cmbAgent.SelectedIndex = 0
    }
    $dlg.Controls.Add($cmbAgent)

    $y += 36
    $lblJob = New-Object System.Windows.Forms.Label
    $lblJob.Text = "Job name:"
    $lblJob.Location = New-Object System.Drawing.Point(16, $y)
    $lblJob.ForeColor = $ThemeTextLight
    $lblJob.AutoSize = $true
    $dlg.Controls.Add($lblJob)

    $txtJob = New-Object System.Windows.Forms.TextBox
    $txtJob.Location = New-Object System.Drawing.Point(120, ($y - 3))
    $txtJob.Size = New-Object System.Drawing.Size(310, 24)
    $dlg.Controls.Add($txtJob)

    $y += 36
    $lblFreq = New-Object System.Windows.Forms.Label
    $lblFreq.Text = "Schedule:"
    $lblFreq.Location = New-Object System.Drawing.Point(16, $y)
    $lblFreq.ForeColor = $ThemeTextLight
    $lblFreq.AutoSize = $true
    $dlg.Controls.Add($lblFreq)

    $cmbFreq = New-Object System.Windows.Forms.ComboBox
    $cmbFreq.Location = New-Object System.Drawing.Point(120, ($y - 3))
    $cmbFreq.Size = New-Object System.Drawing.Size(100, 24)
    $cmbFreq.DropDownStyle = "DropDownList"
    [void]$cmbFreq.Items.AddRange(@("once", "daily", "weekly"))
    $cmbFreq.SelectedIndex = 2
    $dlg.Controls.Add($cmbFreq)

    $lblTime = New-Object System.Windows.Forms.Label
    $lblTime.Text = "Time (IST):"
    $lblTime.Location = New-Object System.Drawing.Point(240, $y)
    $lblTime.ForeColor = $ThemeTextLight
    $lblTime.AutoSize = $true
    $dlg.Controls.Add($lblTime)

    $dtpTime = New-Object System.Windows.Forms.DateTimePicker
    $dtpTime.Location = New-Object System.Drawing.Point(310, ($y - 3))
    $dtpTime.Size = New-Object System.Drawing.Size(120, 24)
    $dtpTime.Format = "Time"
    $dtpTime.ShowUpDown = $true
    $dtpTime.Value = [datetime]::Today.AddHours(9)
    $dlg.Controls.Add($dtpTime)

    $y += 36
    $lblDays = New-Object System.Windows.Forms.Label
    $lblDays.Text = "Weekly days:"
    $lblDays.Location = New-Object System.Drawing.Point(16, $y)
    $lblDays.ForeColor = $ThemeTextLight
    $lblDays.AutoSize = $true
    $dlg.Controls.Add($lblDays)

    $clbDays = New-Object System.Windows.Forms.CheckedListBox
    $clbDays.Location = New-Object System.Drawing.Point(120, $y)
    $clbDays.Size = New-Object System.Drawing.Size(200, 88)
    $clbDays.CheckOnClick = $true
    foreach ($d in @("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")) {
        [void]$clbDays.Items.Add($d, $d -eq "Thursday")
    }
    $dlg.Controls.Add($clbDays)

    $dtpOnce = New-Object System.Windows.Forms.DateTimePicker
    $dtpOnce.Location = New-Object System.Drawing.Point(330, ($y + 8))
    $dtpOnce.Size = New-Object System.Drawing.Size(100, 24)
    $dtpOnce.Format = "Short"
    $dlg.Controls.Add($dtpOnce)

    $chkWake = New-Object System.Windows.Forms.CheckBox
    $chkWake.Text = "Wake PC to run"
    $chkWake.Location = New-Object System.Drawing.Point(330, ($y + 40))
    $chkWake.ForeColor = $ThemeTextSoft
    $chkWake.AutoSize = $true
    $dlg.Controls.Add($chkWake)

    $applyDefaults = {
        $agentId = [string]$cmbAgent.SelectedItem
        $agent = Get-HubAgentById -AgentId $agentId -Catalog $catalog
        if ($agent.defaultJobName) { $txtJob.Text = [string]$agent.defaultJobName }
        if ($hubSettings -and $hubSettings.defaultSchedules.$agentId) {
            $def = $hubSettings.defaultSchedules.$agentId
            if ($def.frequency) {
                $idx = @("once", "daily", "weekly").IndexOf([string]$def.frequency)
                if ($idx -ge 0) { $cmbFreq.SelectedIndex = $idx }
            }
            if ($def.time -match '^(\d{1,2}):(\d{2})$') {
                $dtpTime.Value = [datetime]::Today.AddHours([int]$Matches[1]).AddMinutes([int]$Matches[2])
            }
            if ($def.daysOfWeek) {
                for ($i = 0; $i -lt $clbDays.Items.Count; $i++) {
                    $day = [string]$clbDays.Items[$i]
                    $clbDays.SetItemChecked($i, $day -in @($def.daysOfWeek))
                }
            }
        }
    }

    $cmbAgent.Add_SelectedIndexChanged($applyDefaults)
    & $applyDefaults

    $y += 100
    $lblWsr = New-Object System.Windows.Forms.Label
    $lblWsr.Text = "WSR jobs post to Slack #qa_automation_status_go and Akilandeswari.Sundararajan@paramount.com"
    $lblWsr.Location = New-Object System.Drawing.Point(16, $y)
    $lblWsr.Size = New-Object System.Drawing.Size(420, 32)
    $lblWsr.ForeColor = $ThemeTextSoft
    $dlg.Controls.Add($lblWsr)

    $y += 40
    $btnSave = New-Object System.Windows.Forms.Button
    $btnSave.Text = "Save Windows schedule"
    $btnSave.Location = New-Object System.Drawing.Point(16, $y)
    $btnSave.Size = New-Object System.Drawing.Size(150, 32)
    Set-ActionButtonTheme -Button $btnSave -BackColor $ThemeBtnGreen
    $btnSave.Add_Click({
        try {
            $agentId = [string]$cmbAgent.SelectedItem
            $jobName = $txtJob.Text.Trim()
            if (-not $jobName) { throw "Enter a job name." }
            $cfgPath = Save-HubJobConfig -HubRoot $hubRoot -JobName $jobName -AgentId $agentId `
                -Catalog $catalog -Settings $hubSettings `
                -Frequency $cmbFreq.SelectedItem -Time $dtpTime.Value.ToString("HH:mm") `
                -DaysOfWeek @($clbDays.CheckedItems) -StartDate $dtpOnce.Value.ToString("yyyy-MM-dd")
            Append-Log "Saved job config: $cfgPath"
            $reg = Join-Path $hubRoot "Register-QAAgentSchedule.ps1"
            $args = @("-ConfigPath", $cfgPath)
            if ($chkWake.Checked) { $args += "-WakeComputer" }
            & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $reg @args
            Append-Log "Windows Task Scheduler updated for $jobName."
            [System.Windows.Forms.MessageBox]::Show(
                "Scheduled: $jobName`n`nTask: QAAgentHub-$jobName`n`nUse 'View scheduled jobs' to verify State = Ready.",
                "Schedule saved",
                "OK",
                "Information") | Out-Null
            $dlg.Close()
        } catch {
            Append-Log "ERROR: $($_.Exception.Message)"
            [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Schedule failed", "OK", "Error") | Out-Null
        }
    })
    $dlg.Controls.Add($btnSave)

    $btnRemove = New-Object System.Windows.Forms.Button
    $btnRemove.Text = "Remove schedule"
    $btnRemove.Location = New-Object System.Drawing.Point(176, $y)
    $btnRemove.Size = New-Object System.Drawing.Size(120, 32)
    Set-ActionButtonTheme -Button $btnRemove -BackColor $ThemeBtnPink
    $btnRemove.Add_Click({
        try {
            $jobName = $txtJob.Text.Trim()
            if (-not $jobName) { throw "Enter the job name to remove." }
            $safeName = ($jobName -replace '[^\w\-]', '-').Trim('-')
            $cfgPath = Join-Path $jobsDir "$safeName.json"
            if (-not (Test-Path $cfgPath)) { throw "Job config not found: $cfgPath" }
            $reg = Join-Path $hubRoot "Register-QAAgentSchedule.ps1"
            & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $reg -ConfigPath $cfgPath -Remove
            Append-Log "Removed schedule: $jobName"
        } catch {
            Append-Log "ERROR: $($_.Exception.Message)"
        }
    })
    $dlg.Controls.Add($btnRemove)

    $btnCancel = New-Object System.Windows.Forms.Button
    $btnCancel.Text = "Cancel"
    $btnCancel.Location = New-Object System.Drawing.Point(340, $y)
    $btnCancel.Size = New-Object System.Drawing.Size(90, 32)
    Set-ActionButtonTheme -Button $btnCancel -BackColor $ThemeBtnYellow
    $btnCancel.Add_Click({ $dlg.Close() })
    $dlg.Controls.Add($btnCancel)

    [void]$dlg.ShowDialog($form)
}

function New-AgentTile {
    param(
        [System.Windows.Forms.Control] $Parent,
        [int] $X,
        [int] $Y,
        [int] $Width,
        [int] $Height,
        $Agent
    )

    $tile = New-Object System.Windows.Forms.Panel
    $tile.Location = New-Object System.Drawing.Point($X, $Y)
    $tile.Size = New-Object System.Drawing.Size($Width, $Height)
    $tile.BackColor = Convert-HexToColor ([string]$Agent.color)
    $tile.Cursor = [System.Windows.Forms.Cursors]::Hand
    $tile.Tag = $Agent

    $title = New-Object System.Windows.Forms.Label
    $title.Text = [string]$Agent.title
    $title.Location = New-Object System.Drawing.Point(12, 14)
    $title.Size = New-Object System.Drawing.Size(($Width - 24), 48)
    $title.Font = $ThemeFontTile
    $title.ForeColor = $ThemeBtnText
    $title.BackColor = [System.Drawing.Color]::Transparent
    $tile.Controls.Add($title)

    $sub = New-Object System.Windows.Forms.Label
    $sub.Text = [string]$Agent.subtitle
    $sub.Location = New-Object System.Drawing.Point(12, 62)
    $sub.Size = New-Object System.Drawing.Size(($Width - 24), 56)
    $sub.Font = $ThemeFont
    $sub.ForeColor = [System.Drawing.Color]::FromArgb(40, 40, 50)
    $sub.BackColor = [System.Drawing.Color]::Transparent
    $tile.Controls.Add($sub)

    $baseColor = Convert-HexToColor ([string]$Agent.color)
    $hoverColor = [System.Drawing.Color]::FromArgb(
        [Math]::Min(255, $baseColor.R + 18),
        [Math]::Min(255, $baseColor.G + 18),
        [Math]::Min(255, $baseColor.B + 18))
    $agentCopy = $Agent

    $tile.Add_MouseEnter({
        $tile.BackColor = $hoverColor
    }.GetNewClosure())
    $tile.Add_MouseLeave({
        $tile.BackColor = $baseColor
    }.GetNewClosure())
    $tile.Add_Click({
        try {
            if ([string]$agentCopy.type -eq "submenu") {
                Show-ReleaseLabelPicker -Agent $agentCopy
            } else {
                Invoke-QAAgentPrompt -AgentTitle ([string]$agentCopy.title) -Prompt ([string]$agentCopy.prompt) `
                    -WorkspaceKey ([string]$agentCopy.workspace) -AgentId ([string]$agentCopy.id)
            }
        } catch {
            Append-Log "ERROR: $($_.Exception.Message)"
            [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Launch failed", "OK", "Error") | Out-Null
        }
    }.GetNewClosure())

    $Parent.Controls.Add($tile)
}

function New-QuickLinkTile {
    param(
        [System.Windows.Forms.Control] $Parent,
        [int] $X,
        [int] $Y,
        [int] $Width,
        [int] $Height,
        $Link
    )

    $tile = New-Object System.Windows.Forms.Panel
    $tile.Location = New-Object System.Drawing.Point($X, $Y)
    $tile.Size = New-Object System.Drawing.Size($Width, $Height)
    $tile.BackColor = Convert-HexToColor ([string]$Link.color)
    $tile.Cursor = [System.Windows.Forms.Cursors]::Hand
    $tile.Tag = $Link

    $iconPath = Resolve-HubRelativePath ([string]$Link.icon)
    if ($iconPath -and (Test-Path $iconPath)) {
        try {
            $pic = New-Object System.Windows.Forms.PictureBox
            $pic.Location = New-Object System.Drawing.Point(10, 12)
            $pic.Size = New-Object System.Drawing.Size(48, 48)
            $pic.SizeMode = [System.Windows.Forms.PictureBoxSizeMode]::Zoom
            $pic.BackColor = [System.Drawing.Color]::Transparent
            $pic.Image = [System.Drawing.Image]::FromFile($iconPath)
            $tile.Controls.Add($pic)
        } catch { }
    }

    $titleX = 68
    $title = New-Object System.Windows.Forms.Label
    $title.Text = [string]$Link.title
    $title.Location = New-Object System.Drawing.Point($titleX, 14)
    $title.Size = New-Object System.Drawing.Size(($Width - $titleX - 12), 24)
    $title.Font = $ThemeFontTile
    $title.ForeColor = $ThemeBtnText
    $title.BackColor = [System.Drawing.Color]::Transparent
    $tile.Controls.Add($title)

    $sub = New-Object System.Windows.Forms.Label
    $sub.Text = [string]$Link.subtitle
    $sub.Location = New-Object System.Drawing.Point($titleX, 40)
    $sub.Size = New-Object System.Drawing.Size(($Width - $titleX - 12), 36)
    $sub.Font = $ThemeFont
    $sub.ForeColor = [System.Drawing.Color]::FromArgb(40, 40, 50)
    $sub.BackColor = [System.Drawing.Color]::Transparent
    $tile.Controls.Add($sub)

    $baseColor = Convert-HexToColor ([string]$Link.color)
    $hoverColor = [System.Drawing.Color]::FromArgb(
        [Math]::Min(255, $baseColor.R + 18),
        [Math]::Min(255, $baseColor.G + 18),
        [Math]::Min(255, $baseColor.B + 18))

    $linkRef = $Link
    $openLink = {
        try {
            $scriptPath = Resolve-HubRelativePath ([string]$linkRef.script)
            $launcherPath = Resolve-HubRelativePath ([string]$linkRef.launcher)
            Start-HubExternalApp -AppTitle ([string]$linkRef.title) -ScriptPath $scriptPath -LauncherPath $launcherPath
        } catch {
            Append-Log "ERROR: $($_.Exception.Message)"
            [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, "Launch failed", "OK", "Error") | Out-Null
        }
    }.GetNewClosure()

    $tile.Add_Click($openLink)
    foreach ($child in @($tile.Controls)) {
        $child.Add_Click($openLink)
    }

    $tile.Add_MouseEnter({
        $tile.BackColor = $hoverColor
    }.GetNewClosure())
    $tile.Add_MouseLeave({
        $tile.BackColor = $baseColor
    }.GetNewClosure())

    $Parent.Controls.Add($tile)
}

$form = New-Object System.Windows.Forms.Form
$form.Text = "Media Platform QA Agent Hub"
$form.Size = New-Object System.Drawing.Size(520, 700)
$form.MinimumSize = New-Object System.Drawing.Size(520, 660)
$form.StartPosition = "CenterScreen"
$form.BackColor = $ThemeRoyalBlue
$form.Font = $ThemeFont

$iconIco = Join-Path $hubRoot "assets\qa-agent-hub-icon.ico"
if (Test-Path $iconIco) {
    try { $form.Icon = New-Object System.Drawing.Icon $iconIco } catch { }
}

$lblTitle = New-Object System.Windows.Forms.Label
$lblTitle.Text = [string]$catalog.title
$lblTitle.Location = New-Object System.Drawing.Point(16, 12)
$lblTitle.Size = New-Object System.Drawing.Size(480, 28)
$lblTitle.Font = $ThemeFontTitle
$lblTitle.ForeColor = $ThemeTextLight
$lblTitle.BackColor = [System.Drawing.Color]::Transparent
$form.Controls.Add($lblTitle)

$lblSub = New-Object System.Windows.Forms.Label
$lblSub.Text = [string]$catalog.subtitle
$lblSub.Location = New-Object System.Drawing.Point(16, 40)
$lblSub.Size = New-Object System.Drawing.Size(480, 20)
$lblSub.ForeColor = $ThemeTextSoft
$lblSub.BackColor = [System.Drawing.Color]::Transparent
$form.Controls.Add($lblSub)

$tilePad = 16
$tileGap = 12
$tileW = 228
$tileH = 120
$startY = 72

$agents = @($catalog.agents)
for ($i = 0; $i -lt $agents.Count; $i++) {
    $col = $i % 2
    $row = [Math]::Floor($i / 2)
    $x = $tilePad + ($col * ($tileW + $tileGap))
    $y = $startY + ($row * ($tileH + $tileGap))
    New-AgentTile -Parent $form -X $x -Y $y -Width $tileW -Height $tileH -Agent $agents[$i]
}

$quickLinkH = 72
$quickStartY = $startY + (2 * ($tileH + $tileGap)) + 4
$quickLinks = @($catalog.quickLinks)
for ($i = 0; $i -lt $quickLinks.Count; $i++) {
    $col = $i % 2
    $row = [Math]::Floor($i / 2)
    $x = $tilePad + ($col * ($tileW + $tileGap))
    $y = $quickStartY + ($row * ($quickLinkH + $tileGap))
    New-QuickLinkTile -Parent $form -X $x -Y $y -Width $tileW -Height $quickLinkH -Link $quickLinks[$i]
}

$actionY = $quickStartY + $quickLinkH + 12

$txtLog = New-Object System.Windows.Forms.TextBox
$txtLog.Location = New-Object System.Drawing.Point(16, $actionY)
$txtLog.Size = New-Object System.Drawing.Size(488, 72)
$txtLog.Multiline = $true
$txtLog.ScrollBars = "Vertical"
$txtLog.ReadOnly = $true
$txtLog.BackColor = $ThemeInputBack
$txtLog.ForeColor = $ThemeBtnText
$form.Controls.Add($txtLog)

function Append-Log([string]$msg) {
    $txtLog.AppendText("$msg`r`n")
}

$btnRowY = $actionY + 84
$btnRow2Y = $btnRowY + 40

$btnSchedule = New-Object System.Windows.Forms.Button
$btnSchedule.Text = "Schedule agent"
$btnSchedule.Location = New-Object System.Drawing.Point(16, $btnRowY)
$btnSchedule.Size = New-Object System.Drawing.Size(120, 32)
$btnSchedule.Add_Click({
    try {
        Show-ScheduleAgentDialog
    } catch {
        Append-Log "ERROR: $($_.Exception.Message)"
    }
})
$form.Controls.Add($btnSchedule)
Set-ActionButtonTheme -Button $btnSchedule -BackColor $ThemeBtnGreen

$btnViewJobs = New-Object System.Windows.Forms.Button
$btnViewJobs.Text = "View scheduled jobs"
$btnViewJobs.Location = New-Object System.Drawing.Point(144, $btnRowY)
$btnViewJobs.Size = New-Object System.Drawing.Size(130, 32)
$btnViewJobs.Add_Click({
    try { Show-HubScheduledJobsDialog } catch { Append-Log "ERROR: $($_.Exception.Message)" }
})
$form.Controls.Add($btnViewJobs)
Set-ActionButtonTheme -Button $btnViewJobs -BackColor $ThemeBtnYellow

$btnPin = New-Object System.Windows.Forms.Button
$btnPin.Text = "Pin to taskbar"
$btnPin.Location = New-Object System.Drawing.Point(16, $btnRow2Y)
$btnPin.Size = New-Object System.Drawing.Size(120, 32)
$btnPin.Add_Click({
    try {
        $install = Join-Path $hubRoot "Register-QAAgentHubHotkey.ps1"
        & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $install
        Append-Log "Shortcut + hotkey installed (Ctrl+Shift+H)."
        [System.Windows.Forms.MessageBox]::Show(
            "Desktop + Start Menu shortcuts created.`n`nGlobal hotkey: Ctrl+Shift+H opens QA Agent Hub.`n`nPin to taskbar:`n1. Open Start Menu -> QA Agent Hub`n2. Right-click -> Pin to taskbar",
            "QA Agent Hub",
            "OK",
            "Information") | Out-Null
    } catch {
        Append-Log "ERROR: $($_.Exception.Message)"
    }
})
$form.Controls.Add($btnPin)
Set-ActionButtonTheme -Button $btnPin -BackColor $ThemeBtnPink

$btnClose = New-Object System.Windows.Forms.Button
$btnClose.Text = "Close"
$btnClose.Location = New-Object System.Drawing.Point(360, $btnRow2Y)
$btnClose.Size = New-Object System.Drawing.Size(144, 32)
$btnClose.Add_Click({ $form.Close() })
$form.Controls.Add($btnClose)
Set-ActionButtonTheme -Button $btnClose -BackColor $ThemeBtnYellow

Append-Log "Ready - click a tile to run now, or open Scheduler / Heidi tiles above."
Append-Log "Global hotkey: Ctrl+Shift+H opens this hub (after Pin to taskbar)."
Append-Log "WSR sends to Slack #qa_automation_status_go + Akilandeswari.Sundararajan@paramount.com"

[void]$form.ShowDialog()
