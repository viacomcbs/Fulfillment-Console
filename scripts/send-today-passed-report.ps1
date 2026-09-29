# Send today's passed test cases report from existing allure-results.
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

mvn -q test-compile exec:java `
  "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.paramount.test.ff.common.util.reporting.TodayPassedReportSender" `
  "-DsuiteXmlFile=src/test/resources/TestNGSuiteConfig.xml"
