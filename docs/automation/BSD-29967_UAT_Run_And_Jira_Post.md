# BSD-29967 — UAT runs + Jira evidence post

**Story:** [BSD-29967](https://paramount.atlassian.net/browse/BSD-29967)  
**Suite XML (UAT):** `src/test/resources/insprint-automation/BSD-29967_UAT_ProdServerSuite.xml`  
**Suite XML (PROD round-robin):** `src/test/resources/insprint-automation/BSD-29967_ProdServerSuite.xml`  
**Accumulated results:** `test-output/jira-evidence/BSD-29967_workflow-results-accumulated.json`

---

## Run on UAT (Synergy / IntelliJ)

Right-click the suite XML **or** use PowerShell from repo root:

```powershell
mvn -B test `
  "-DsuiteXmlFile=src/test/resources/insprint-automation/BSD-29967_UAT_ProdServerSuite.xml"
```

- **FC URL:** UAT (`uat-operationsconsole.paramountmsc.com/fulfillment/`)
- **Environment workflows:** UWFFSP + UWFPARAMOUNT only (fixed in UAT suite XML)
- **Calendar:** Today (UAT suite `Bsd29967CalendarPreset=Today`)
- **Ops-console (UAT):** `https://uat.contentplatform.viacom.com/ops-console-api-dev-ui/order/{orderId}`
- **Ops-console (DEV/PROD FC):** `https://contentplatform.viacom.com/ops-console-api-dev-ui/order/{orderId}` (unchanged)

---

## 3–4 runs, then one Jira post

Each **PASS** run merges workflow rows (with populated DSIDs) into the accumulated JSON.  
Do **not** post to Jira on every run.

| Run | `AttachJiraEvidenceOnPass` | What happens |
|-----|----------------------------|--------------|
| 1–3 | `false` | Results merge locally; HTML saved under `test-output/jira-evidence/` |
| After run 3 or 4 | Manual post (below) | One comment + combined HTML attachment on BSD-29967 |

### Runs 1–4 (accumulate only)

Repeat the Maven command above **3 or 4 times** (or use GHA **In-Sprint Automation** → `BSD-29967`, environment **UAT**).

Optional helper script:

```powershell
.\scripts\run-bsd-29967.ps1 -Environment UAT -Runs 4
```

---

## Post to Jira (once, after accumulated runs)

Set Jira API credentials (Atlassian API token for your `@paramount.com` account):

```powershell
$env:JIRA_API_EMAIL = "you@paramount.com"
$env:JIRA_API_TOKEN = "your-atlassian-api-token"
```

Publish **all accumulated PASS workflows**:

```powershell
mvn -q exec:java "-Dexec.classpathScope=test" `
  "-Dexec.mainClass=com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967JiraManualPost"
```

This adds a comment on [BSD-29967](https://paramount.atlassian.net/browse/BSD-29967) and attaches the combined workflow DSID table HTML.

**Alternative:** set `-Dsystem.test.attachjiraevidenceonpass=true` on the **last** run only (with credentials set) — auto-posts that run’s merged table.

---

## GHA (optional)

Workflow: **In-Sprint Automation** → Jira key **BSD-29967** → **UAT** → your email.

---

## Email report

Suite sends email when `SendReportAutoEmails=true` (default in XML). Override recipient:

`-Dsystem.test.sendreportemailaddress=you@paramount.com`
