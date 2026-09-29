@echo off
REM Pin this file to the Windows taskbar for one-click QA Agent Hub (like Teams / Excel).
cd /d "%~dp0.."
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Launch-QAAgentHub.ps1"
