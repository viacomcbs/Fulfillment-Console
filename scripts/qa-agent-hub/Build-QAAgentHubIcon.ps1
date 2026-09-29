# Builds qa-agent-hub-icon.ico (purple agent tile) for Windows shortcuts/taskbar.
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$assets = Join-Path $PSScriptRoot "assets"
New-Item -ItemType Directory -Force -Path $assets | Out-Null
$icoPath = Join-Path $assets "qa-agent-hub-icon.ico"

$size = 256
$bmp = New-Object System.Drawing.Bitmap $size, $size
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::FromArgb(28, 58, 138))

$brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(156, 39, 176))
$g.FillEllipse($brush, 40, 40, 176, 176)

$white = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$font = New-Object System.Drawing.Font("Segoe UI", 72, [System.Drawing.FontStyle]::Bold)
$format = New-Object System.Drawing.StringFormat
$format.Alignment = [System.Drawing.StringAlignment]::Center
$format.LineAlignment = [System.Drawing.StringAlignment]::Center
$rect = New-Object System.Drawing.RectangleF 0, 0, $size, $size
$g.DrawString("QA", $font, $white, $rect, $format)

$icon = [System.Drawing.Icon]::FromHandle($bmp.GetHicon())
$fs = [System.IO.File]::Open($icoPath, [System.IO.FileMode]::Create)
try {
    $icon.Save($fs)
} finally {
    $fs.Close()
    $g.Dispose()
    $bmp.Dispose()
}

Write-Host "Wrote $icoPath"
