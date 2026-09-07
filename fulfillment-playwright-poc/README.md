# Fulfillment Console — Playwright Python PoC

Minimal proof-of-concept for **BSD-29582 (Table Refresh)** and **BSD-29870 (DSID)** only.

Does **not** replace the Java/Synergy suite. Runs locally with **Playwright + pytest** (no Synergy agent).

## PoC scope

### In scope (9 tests)

| ID | Java equivalent | Tab |
|----|-----------------|-----|
| FF_TR_001 | `TR_O_001_ValidateRefreshButtonVisible` | Orders |
| FF_TR_002 | `TR_O_002_ValidateRefreshCallsFilterOrderApi` | Orders |
| FF_TR_003 | `TR_O_003_ValidateRefreshNoDuplicateRows` | Orders |
| FF_TR_004 | `TR_LI_001_ValidateRefreshButtonVisible` | Line Items |
| FF_TR_005 | `TR_LI_002_ValidateRefreshCallsLineItemViewLandingApi` | Line Items |
| FF_TR_006 | `TR_LI_003_ValidateRefreshNoDuplicateRows` | Line Items |
| FF_DSID_001 | `FF_DSID_O_001_ValidateAtLeastOneOrderShowsDsid` | Orders |
| FF_DSID_002 | `FF_DSID_O_002_ValidateTableDsidMatchesDetailsPanelLastDsid` | Orders |
| FF_DSID_003 | `FF_DSID_LI_001_ValidateLineItemsDsidTableMatchesDetails` | Line Items |

### Out of scope (for now)

- Left-filter matrix (~313 tests)
- Synergy server / local agent
- Email reports, TestRail posting, S3 Allure upload
- Feature-flag / PTS packaging suites

## Prerequisites

- Python 3.10+
- Chrome (Playwright installs Chromium automatically)

```powershell
cd fulfillment-playwright-poc
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
playwright install chromium
copy .env.example .env
# Edit .env with your credentials and PROD/UAT URL
```

## Login-only smoke (service account, no Okta push)

**IntelliJ:** Run → **FF Login Service Account (Playwright)**

**Suite XML:** `suite/FF_Login_ProdServerSuite.xml` (credentials + PROD URL)

**Terminal:**
```powershell
pytest tests/test_login_service_account.py --headed -s -v
```

Uses `mscbsdqasvc@msc.cbs.net` — same as Java `*ProdServerSuite.xml`. No Okta Verify push.

> **Note:** Playwright opens Chrome on your machine from IntelliJ. This uses **server login mode** (service account). It does not provision a Synergy cloud device unless you later add the Synergy Python SDK.

## Run

```powershell
# All PoC tests (headed — Okta push may require manual approve)
pytest tests/ --headed -s

# Table Refresh only
pytest tests/tablerefresh/ -m tablerefresh --headed

# DSID only
pytest tests/dsid/ -m dsid --headed

# Single test
pytest tests/tablerefresh/test_tr_orders_001_refresh_button_visible.py --headed -s
```

## Environment

Copy `.env.example` → `.env`:

| Variable | Example |
|----------|---------|
| `FF_ENV` | `PROD` or `UAT` |
| `FF_USERNAME` | your `@paramount.com` email |
| `FF_PASSWORD` | Okta password |
| `FF_HEADLESS` | `false` (use headed for Okta Verify push) |

URLs are in `config/environments.yaml`.

## Project layout

```
fulfillment-playwright-poc/
├── config/environments.yaml   # DEV/UAT/PROD URLs
├── conftest.py                  # session browser + login fixture
├── pages/                       # Page Object Model (XPath from Java)
├── helpers/                     # network capture, calendar, session flags
└── tests/
    ├── tablerefresh/            # 6 Refresh tests
    └── dsid/                    # 3 DSID tests
```

## Okta login note

First run opens Fulfillment Console and steps through Okta. **Approve the Okta Verify push** on your phone when prompted. Session is reused for subsequent tests in the same pytest run.

## Next steps after PoC

1. Port `LeftFilterPanelUtil` filter expand/select helpers (needed for DSID filter setup).
2. Port `FeatureFlagColumnUtil` manage-columns flow (DSID column enable).
3. Add CI job (GitHub Actions) with headed=false + stored session/cookies if MQE supports it.
4. Wire Allure + TestRail IDs from Java `*TestRailCaseIds` classes.

## Mapping from Java

| Java | Python |
|------|--------|
| `TableRefreshOrdersBaseTest` | `tests/tablerefresh/conftest.py` → `orders_session` fixture |
| `DsidOrdersBaseTest` | `tests/dsid/conftest.py` → `dsid_orders_session` fixture |
| `TableRefreshNetworkUtil` | `helpers/network_capture.py` (Playwright `page.on("request")`) |
| `By.XPath(...)` | `page.locator("xpath=...")` |
| `keepDriverAliveAfterTestMethod` | pytest `scope="session"` fixtures |
