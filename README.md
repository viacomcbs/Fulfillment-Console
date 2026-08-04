# Fulfillment Console Automation

Synergy + TestNG automation for **Fulfillment Console** (Paramount MSC Operations Console).

| Item | Value |
|------|--------|
| Jira story (PTS) | BSD-29441 |
| TestRail project | 223 |
| Framework | Java 11, TestNG, Synergy 5.x, Allure |
| PROD PTS suite | `src/test/resources/FF_PTS_BSD29441_ProdServerSuite.xml` |

## Prerequisites

- JDK 11+
- Maven 3.6+
- Synergy client (local `localhost:39445/synergy` or Synergy Public Devices)
- Network access to `synergyserver.tech` and Fulfillment Console URLs

## Run PTS Orders (PROD)

**IntelliJ:** Right-click `FF_PTS_BSD29441_ProdServerSuite.xml` → Run

```powershell
mvn test "-DsuiteXmlFile=src/test/resources/FF_PTS_BSD29441_ProdServerSuite.xml"
```

Tests: O_001 → O_002 → O_011 → O_009 → O_015 → O_013 → O_017

## TestRail + Git automation

1. Import manual cases: `docs/testrail/FF_PTS_Orders_ManualCases_Import.csv`
2. Replace `C_TODO_*` in `PtsTestRailCaseIds.java` with real TestRail case IDs
3. Link Git repo in TestRail (Project 223 → Settings → Automation)
4. Full guide: `docs/testrail/PTS_TestRail_Linking_Guide.md`
5. Mapping JSON: `docs/testrail/automation-mapping.json`

Each automated test uses `@TmsLink` + constants in `com.paramount.test.ff.uitests.testrail.PtsTestRailCaseIds`.

## Git — first push

```powershell
cd FF_Console_Automation
git add .
git commit -m "Initial Fulfillment Console automation with BSD-29441 PTS Orders suite"
git remote add origin <YOUR_REPO_URL>
git push -u origin master
```

Use your team Git host (GitHub Enterprise `viacomcbs`, Bitbucket, etc.). Ask QA lead for the empty repo URL if one does not exist yet.

## Security note

Suite XML files may contain service credentials. Prefer team secrets / CI variables for shared repos. Do not commit personal passwords.

## Project layout

```
src/test/java/          Test classes & helpers
src/test/resources/     TestNG suites, elements, test data
docs/                   Manual test specs & TestRail import
scripts/                Jira evidence packaging
Jenkinsfile             CI pipeline template
```
