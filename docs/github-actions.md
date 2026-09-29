# GitHub Actions — Fulfillment Console automation

Use GitHub Actions as a Jenkins-style CI runner for Synergy + TestNG suites.

## How this maps to Jenkins

| Jenkins | GitHub Actions |
|---------|----------------|
| Pipeline job | Workflow (`.github/workflows/*.yml`) |
| Build parameters (`TEST_ENVIRONMENT`, `TestNGSuiteConfig`) | `workflow_dispatch` inputs |
| Agent label (`generic_v3`) | `runs-on: [self-hosted, linux, synergy]` |
| `mvn clean test -DsuiteXmlFile=...` | Same Maven command in a workflow step |
| Allure / TestNG report stage | `upload-artifact` + optional Allure GitHub Pages |
| Credentials (`svc.bvc_map`, settings.xml) | Repository secrets + `settings.xml` on runner |

## One-time setup

### 1. Register a self-hosted runner

Synergy tests need:

- Outbound access to `synergyserver.tech`, Fulfillment Console URLs, and `nexus.mtvi.com`
- JDK 11 (project compiles with Java 8 target; avoid JDK 24 locally)
- Maven 3.6+

GitHub → repo **Settings → Actions → Runners → New self-hosted runner**. Tag the runner with labels used in the workflow, e.g. `self-hosted`, `linux`, `synergy`.

Public `ubuntu-latest` runners usually **cannot** reach internal Nexus; use a Paramount/Viacom self-hosted runner (same idea as Jenkins agents).

### 2. Add repository secrets (recommended)

**Settings → Secrets and variables → Actions**

| Secret | Purpose |
|--------|---------|
| `FF_USERNAME` | Fulfillment Console service account |
| `FF_PASSWORD` | Service account password |
| `SYNERGY_USER_KEY` | Synergy lab user key |

Long term, move credentials out of suite XML and read them via `ConfigProps` / system properties. Until then, suite XML parameters still work on trusted runners.

### 3. Push the workflow

Workflow file: `.github/workflows/order-status-left-filter-prod.yml`

After push to `main`/`master`, open **Actions** tab in GitHub.

## Run Orders-view left-filter regression (Core 8 filters, PROD)

Workflow: **Left Filter Regression — Orders Core 8 PROD**  
File: `.github/workflows/left-filter-regression-orders-prod.yml`

Suite: `src/test/resources/regression/left-filters/orders-view/LF_O_Core8_LeftFilters_ProdServerSuite.xml`

**Included filters (61 tests, one browser session):**

| Filter | Tests |
|--------|------:|
| Activity Type | 8 |
| Assigned To | 8 |
| Brand | 8 |
| Environment | 8 |
| Flag | 8 |
| Line Item Status | 8 |
| Order Status | 7 |
| Submitted By | 8 |

**Excluded for now:** Error Message (run separately when UAT/PROD data is stable).

### Reports

- **Email:** HTML scenario table via `SendReportAutoEmails=true` (same as local Synergy runs)
- **Slack:** Summary posted when `SendChatReport=true` and secrets are set (via Synergy `SlackMessenger`)

### Repository secrets (Actions → Secrets)

| Secret | Purpose |
|--------|---------|
| `SLACK_WEBHOOK_URL` | Incoming webhook for Slack channel (ask lead / Ravi for URL) |
| `SYNERGY_USER_KEY` | Optional override of suite XML UserKey |
| `FF_USERNAME` / `FF_PASSWORD` | Optional — move credentials out of suite XML later |

**Variables (optional):** set `SLACK_CHANNEL` on the runner env, or pass **slack_channel** when using **Run workflow**.

Maven/Nexus: self-hosted runner must reach `nexus.mtvi.com` or have `~/.m2` pre-populated (same as Jenkins `settings.xml`).

### Manual run (GitHub Actions)

1. **Actions** → **Left Filter Regression — Orders Core 8 PROD** → **Run workflow**
2. Branch: your feature branch (e.g. `Akila_FulfillmentConsole`)
3. **email:** report recipient
4. **slack_channel:** e.g. `#your-team-channel`
5. **Run workflow**

Scheduled: **weekdays 11:00 UTC** (adjust cron in workflow after lead confirms time).

### Local / Maven (single suite, PROD)

```powershell
.\scripts\run-orders-left-filter-regression-all.ps1 -Email "you@paramount.com" -Environment PROD
```

Or:

```powershell
mvn test "-DsuiteXmlFile=src/test/resources/regression/left-filters/orders-view/LF_O_Core8_LeftFilters_ProdServerSuite.xml"
```

Full suite including Error Message: `LF_O_All_LeftFilters_ProdServerSuite.xml` (69 tests).

---

## Legacy: per-filter suite XMLs (removed)

Per-filter files such as `LF_O_Flag_All_ProdServerSuite.xml` were consolidated. Regenerate suites with:

```powershell
python scripts/generate_regression_suite_xml.py
```

## Run Order Status left-filter tests (parallel)

The workflow **Order Status Left Filter PROD** runs **two jobs in parallel**:

| Job | Suite | Tests |
|-----|-------|-------|
| Orders Order Status | `regression/left-filters/orders-view/LF_O_OrderStatus_All_ProdServerSuite.xml` | TC101–TC1101 (8 tests, one browser session) |
| Line Items Order Status | `LF_LI_OrderStatus_All_ProdServerSuite.xml` | TC102–TC1102 (**excludes TC602** table sync) |

### Manual run (like “Build with Parameters”)

1. **Actions** → **Order Status Left Filter PROD** → **Run workflow**
2. Choose branch, `PROD` / `UAT` / `DEV`, email recipient
3. **Run workflow**

Each matrix job is a **separate JVM** (safe for shared left-filter session state). Do **not** use TestNG `parallel="tests"` for Orders + Line Items in one suite — `LeftFilterSessionHelper` uses static flags.

### Local / Maven (single suite)

```powershell
# Orders — all 8 Order Status tests, sequential, shared session
mvn test "-DsuiteXmlFile=src/test/resources/regression/left-filters/orders-view/LF_O_OrderStatus_All_ProdServerSuite.xml"

# Line items — 7 tests (no TC602 table sync)
mvn test "-DsuiteXmlFile=src/test/resources/LF_LI_OrderStatus_All_ProdServerSuite.xml"
```

Run both locally in parallel: open two terminals and run one command in each.

## Artifacts and reports

After each job:

- `allure-results/` — Allure raw results
- `target/surefire-reports/` — TestNG XML/HTML
- `test-output/screenshots/` — assertion screenshots

Download from the workflow run **Artifacts** section. Email reports still send via existing `SuiteListeners` / `LeftFilterSuiteListener` when SMTP is reachable from the runner.

## Optional: generic workflow for any suite

Duplicate the workflow and change `suite_xml`, or add a string input:

```yaml
suite_xml:
  description: TestNG suite path under src/test/resources
  default: regression/left-filters/orders-view/LF_O_OrderStatus_All_ProdServerSuite.xml
```

Maven step:

```bash
mvn -B test "-DsuiteXmlFile=src/test/resources/${{ inputs.suite_xml }}"
```

This mirrors Jenkins parameter `TestNGSuiteConfig`.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Maven cannot resolve `com.synergy.server` | Runner needs Nexus access or pre-populated `~/.m2` |
| Synergy session fails | Check `UserKey`, network, Synergy Public Devices availability |
| Allure upload to Synergy NPE | Known Synergy server issue; email report still works |
| JDK warnings with Corretto 24 | Use JDK 11 on CI and locally for this project |
