# Scheduled Automation (IST)

Runs from the **Media Platform Automation Scheduler** project (sibling to this repo).

When this workflow is hosted in the scheduler repo, check out both repositories:

1. This scheduler repo (workflow + job JSON + `run-from-config.sh`)
2. Fulfillment Console automation repo at `./Fulfillment-Console`

Set `AUTOMATION_REPO_ROOT` to the test repo path before running Maven.

## Local project path (Akila machine)

```
C:\FulfillmentConsole\Media-Platform-Automation-Scheduler
C:\FulfillmentConsole\Fulfillment-Console
```

Launch GUI:

```powershell
cd C:\FulfillmentConsole\Media-Platform-Automation-Scheduler
.\Launch-AutomationScheduler.ps1
```

Full docs: `Media-Platform-Automation-Scheduler\docs\SCHEDULER.md`
