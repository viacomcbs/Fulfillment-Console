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

## Run Orders-view left-filter regression (8 suite XMLs)

Workflow: **Left Filter Regression — Orders View**  
File: `.github/workflows/left-filter-regression-orders-prod.yml`

Suites under `src/test/resources/regression/left-filters/orders-view/`:

| Filter | Suite XML |
|--------|-----------|
| Activity Type | `LF_O_ActivityType_All_ProdServerSuite.xml` |
| Assigned To | `LF_O_AssignedTo_All_ProdServerSuite.xml` |
| Brand | `LF_O_Brand_All_ProdServerSuite.xml` |
| Environment | `LF_O_Environment_All_ProdServerSuite.xml` |
| Flag | `LF_O_Flag_All_ProdServerSuite.xml` |
| Line Item Status | `LF_O_LineItemStatus_All_ProdServerSuite.xml` |
| Order Status | `LF_O_OrderStatus_All_ProdServerSuite.xml` |
| Submitted By | `LF_O_SubmittedBy_All_ProdServerSuite.xml` |

### Step 1 — run one suite first (recommended)

1. **Actions** → **Left Filter Regression — Orders View** → **Run workflow**
2. Branch: **`Akila_FulfillmentConsole`** (or your feature branch)
3. **run_mode:** `single`
4. **suite_xml:** pick one filter, e.g. `LF_O_Flag_All_ProdServerSuite.xml`
5. **test_environment:** `PROD`
6. **email:** your report address
7. **Run workflow**

You get one job, one email with scenario table, one Allure artifact.

### Step 2 — run all 8 suites in parallel

Same workflow, set **run_mode:** `all_parallel`.  
GitHub starts **8 jobs** (one JVM per filter — safe for shared left-filter session).

Each job needs a self-hosted runner with label `synergy`. If you only have **one** runner, jobs queue and run one after another (still works, just slower).

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
