# Background global hotkey listener for QA Agent Hub.
# Default: Ctrl+Shift+H

param(
    [switch] $UseCtrl,
    [switch] $UseAlt,
    [switch] $UseShift,
    [ValidateSet(
        "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
        "F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12"
    )]
    [string] $Key = "H"
)

$ErrorActionPreference = "Stop"
$hubRoot = $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $hubRoot "..\..")).Path
$launcherCmd = Join-Path $repoRoot "scripts\QA Agent Hub.cmd"

if (-not (Test-Path $launcherCmd)) {
    throw "Launcher not found: $launcherCmd"
}

if (-not $UseCtrl -and -not $UseAlt -and -not $UseShift) {
    $UseCtrl = $true
    $UseShift = $true
}

Add-Type @"
using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Windows.Forms;

public static class HubHotkeyNative {
    public const int WM_HOTKEY = 0x0312;
    public const int MOD_ALT = 0x0001;
    public const int MOD_CONTROL = 0x0002;
    public const int MOD_SHIFT = 0x0004;
    public const int HOTKEY_ID = 0x5141;

    [DllImport("user32.dll", SetLastError = true)]
    public static extern bool RegisterHotKey(IntPtr hWnd, int id, uint fsModifiers, uint vk);

    [DllImport("user32.dll", SetLastError = true)]
    public static extern bool UnregisterHotKey(IntPtr hWnd, int id);
}

public class HubHotkeyForm : Form {
    private readonly string launcherPath;

    public HubHotkeyForm(string launcherPath, uint modifiers, uint virtualKey) {
        this.launcherPath = launcherPath;
        this.ShowInTaskbar = false;
        this.WindowState = FormWindowState.Minimized;
        this.FormBorderStyle = FormBorderStyle.FixedToolWindow;
        this.Opacity = 0;
        this.Load += (sender, args) => {
            if (!HubHotkeyNative.RegisterHotKey(this.Handle, HubHotkeyNative.HOTKEY_ID, modifiers, virtualKey)) {
                throw new InvalidOperationException("Could not register global hotkey. It may already be in use.");
            }
        };
        this.FormClosed += (sender, args) => {
            HubHotkeyNative.UnregisterHotKey(this.Handle, HubHotkeyNative.HOTKEY_ID);
        };
    }

    protected override void WndProc(ref Message m) {
        if (m.Msg == HubHotkeyNative.WM_HOTKEY && m.WParam.ToInt32() == HubHotkeyNative.HOTKEY_ID) {
            try {
                Process.Start(new ProcessStartInfo {
                    FileName = launcherPath,
                    WorkingDirectory = System.IO.Path.GetDirectoryName(launcherPath),
                    UseShellExecute = true
                });
            } catch { }
            return;
        }
        base.WndProc(ref m);
    }
}
"@

function Get-VirtualKeyCode {
    param([string] $KeyName)
    if ($KeyName -match '^F(\d{1,2})$') {
        return [uint32](0x70 + ([int]$Matches[1] - 1))
    }
    return [uint32][System.Windows.Forms.Keys]::$KeyName
}

$modifiers = 0
if ($UseAlt) { $modifiers = $modifiers -bor [HubHotkeyNative]::MOD_ALT }
if ($UseCtrl) { $modifiers = $modifiers -bor [HubHotkeyNative]::MOD_CONTROL }
if ($UseShift) { $modifiers = $modifiers -bor [HubHotkeyNative]::MOD_SHIFT }

$vk = Get-VirtualKeyCode -KeyName $Key
$form = New-Object HubHotkeyForm $launcherCmd, [uint32]$modifiers, $vk
[void][System.Windows.Forms.Application]::Run($form)
