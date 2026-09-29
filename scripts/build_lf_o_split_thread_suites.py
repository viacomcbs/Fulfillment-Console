#!/usr/bin/env python3
"""
Classify Orders left-filter folders by execution history, generate per-filter session
suites, and write a split manifest for two-thread parallel PROD runs.

Thread 1: filters with at least one prior LF_O test execution.
Thread 2: filters never executed.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "scripts"))
from generate_regression_suite_xml import discover_tests  # noqa: E402

JAVA_ORDERS = (
    PROJECT_ROOT
    / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter/orders"
)
SESSIONS_DIR = PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/sessions"
HISTORY_JSON = PROJECT_ROOT / "docs/test-tracking/perfilter_execution_history.json"
MANIFEST_JSON = PROJECT_ROOT / "test-output/lf-o-split-manifest.json"
COMBINED_SUITE = SESSIONS_DIR / "LF_O_Split_CombinedEmail_ProdServerSuite.xml"

SKIP_DIRS = {"aggregated", "jobtype"}  # jobtype/ duplicates job/ (already executed)
SUITE_TITLE = "Orders Tab - Left Filters Regression (PROD)"
STAKEHOLDER_EMAILS = (
    "Akilandeswari.Sundararajan@paramount.com,"
    "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
)
INTERNAL_EMAILS = "Akilandeswari.Sundararajan@paramount.com"
SLACK_CHANNEL = "#qa_automation_status_go"
AGGREGATE_DIR = "test-output/lf-o-split-aggregate"

FILTER_LABELS = {
    "activitytype": "Activity Type",
    "assignedto": "Assigned To",
    "brand": "Brand",
    "contenttype": "Content type",
    "deliveryprotocol": "Delivery Protocol",
    "demandsystem": "Demand system",
    "environment": "Environment",
    "episodenumber": "Episode number",
    "errormessage": "Error message",
    "flag": "Flag",
    "franchise": "Franchise",
    "job": "Job type",
    "jobtype": "Job type (legacy)",
    "language": "Language",
    "lineitemstatus": "Line Item Status",
    "orderstatus": "Order Status",
    "partner": "Partner",
    "region": "Region",
    "seasonnumber": "Season number",
    "seriestitle": "Series title",
    "submittedby": "Submitted by",
    "systemname": "System Name",
}


def load_history_runs() -> list[dict]:
    if not HISTORY_JSON.is_file():
        return []
    data = json.loads(HISTORY_JSON.read_text(encoding="utf-8"))
    if isinstance(data, list):
        return data
    return data.get("runs", data.get("records", []))


def list_filter_dirs() -> list[str]:
    return sorted(
        d.name
        for d in JAVA_ORDERS.iterdir()
        if d.is_dir() and d.name not in SKIP_DIRS
    )


def filter_test_classes(filter_dir: str) -> set[str]:
    folder = JAVA_ORDERS / filter_dir
    return {p.stem for p in folder.glob("LF_O_*.java") if p.is_file()}


def is_filter_executed(filter_dir: str, runs: list[dict]) -> bool:
    classes = filter_test_classes(filter_dir)
    if not classes:
        return False
    for run in runs:
        cls = run.get("testClass", "")
        if cls in classes:
            return True
    return False


def discover_filter_tests(filter_dir: str) -> list[tuple[str, str]]:
    folder = JAVA_ORDERS / filter_dir
    flow_tests = sorted(folder.glob("LF_O_Flow_*.java"))
    if flow_tests:
        text = flow_tests[0].read_text(encoding="utf-8")
        method_match = re.search(r"public\s+void\s+(flow_\w+)\s*\(", text)
        if method_match:
            return [(flow_tests[0].stem, method_match.group(1))]
    rows = [(f, cls, method) for f, cls, method in discover_tests("orders", [filter_dir]) if f == filter_dir]
    rows = [(cls, method) for _, cls, method in rows if not cls.startswith("LF_O_TC999_")]
    rows.sort(key=lambda r: (r[0], r[1]))
    return rows


def session_header(filter_dir: str, filter_label: str, maven_path: str, suite_name: str) -> str:
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Orders left-filter session on PROD Synergy — auto-generated split-thread suite.
     Filter: {filter_label}. Maven: mvn test "-DsuiteXmlFile={maven_path}" -->
<suite name="{suite_name}" parallel="false" preserve-order="true">

    <parameter name="RunAsFactory" value="false"/>
    <parameter name="Browser" value="Chrome"/>
    <parameter name="OS" value="Windows"/>
    <parameter name="OSVersion" value="11"/>
    <parameter name="DeviceCategory" value="Desktop"/>
    <parameter name="TestType" value="Regression"/>
    <parameter name="GuiType" value="Web"/>
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SendChatReport" value="false"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{SUITE_TITLE}"/>

    <parameter name="Application" value="Fulfillment Console"/>
    <parameter name="ApplicationTitle" value="Fulfillment Console"/>
    <parameter name="TestEnvironment" value="PROD"/>

    <parameter name="LabUrl" value="https://www.synergyserver.tech"/>
    <parameter name="UserKey" value="af27f06e-2e7e-4c8d-9312-2320423e4641"/>
    <parameter name="LocalURL" value="http://localhost:39445/synergy"/>
    <parameter name="LocalExecution" value="false"/>
    <parameter name="ClientID" value="5CG23257SH"/>

    <parameter name="TargetUrlDEV" value="https://dev-operationsconsole.paramountmsc.com/fulfillment/"/>
    <parameter name="TargetUrlUAT" value="https://uat-operationsconsole.paramountmsc.com/fulfillment/"/>
    <parameter name="TargetUrlPROD" value="https://operationsconsole.paramountmsc.com/fulfillment/"/>

    <parameter name="Username" value="svc-msc_bsd_qa_svc@paramount.com"/>
    <parameter name="Password" value="D7h3M3Bo9SH4vJowwCXku2737!"/>

    <parameter name="PathToElements" value="/src/test/resources/elements/"/>
    <parameter name="PageLoadWaitTime" value="120"/>
    <parameter name="EmailSenderAddress" value="NOREPLY@dteqa.com"/>

    <listeners>
        <listener class-name="com.paramount.test.ff.common.listeners.AllureListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>
    </listeners>
"""


def build_filter_session_suite(filter_dir: str) -> tuple[Path, int]:
    filter_label = FILTER_LABELS.get(filter_dir, filter_dir.replace("_", " ").title())
    file_name = f"LF_O_Split_{filter_dir}_ProdServerSuite.xml"
    out_path = SESSIONS_DIR / file_name
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    suite_name = f"LF Orders Split {filter_label} PROD"
    tests = discover_filter_tests(filter_dir)
    if not tests:
        raise RuntimeError(f"No LF_O tests found for filter folder: {filter_dir}")

    lines = [session_header(filter_dir, filter_label, maven_path, suite_name)]
    lines.append(f'    <test verbose="1" name="{filter_label} - left filters" preserve-order="true">')
    lines.append("        <classes>")
    for class_name, method_name in tests:
        fqcn = (
            f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders."
            f"{filter_dir}.{class_name}"
        )
        lines.append(f'            <class name="{fqcn}">')
        lines.append(f'                <methods><include name="{method_name}"/></methods>')
        lines.append("            </class>")
    lines.append("        </classes>")
    lines.append("    </test>")
    lines.append("</suite>")
    lines.append("")
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines), encoding="utf-8")
    return out_path, len(tests)


def build_combined_email_suite() -> None:
    maven_path = COMBINED_SUITE.relative_to(PROJECT_ROOT).as_posix()
    content = f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Dual email after run-lf-orders-split-parallel-prod.ps1 -->
<suite name="LF Orders Split Combined Email PROD" parallel="false" preserve-order="true">
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SkipSuiteFinishAutoEmail" value="true"/>
    <parameter name="SendReportEmailAddress" value="{STAKEHOLDER_EMAILS}"/>
    <parameter name="SendReportInternalEmailAddress" value="{INTERNAL_EMAILS}"/>
    <parameter name="SendChatReport" value="true"/>
    <parameter name="SlackChannel" value="{SLACK_CHANNEL}"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{SUITE_TITLE}"/>
    <parameter name="AggregateResultsDir" value="{AGGREGATE_DIR}"/>
    <parameter name="Application" value="Fulfillment Console"/>
    <parameter name="ApplicationTitle" value="Fulfillment Console"/>
    <parameter name="TestEnvironment" value="PROD"/>
    <parameter name="LocalExecution" value="true"/>
    <parameter name="LabUrl" value="https://www.synergyserver.tech"/>
    <parameter name="UserKey" value="af27f06e-2e7e-4c8d-9312-2320423e4641"/>
    <parameter name="EmailRelayHost" value="imailrelay.viacom.com"/>
    <parameter name="EmailRelayPort" value="25"/>
    <parameter name="EmailSenderAddress" value="NOREPLY@dteqa.com"/>
    <listeners>
        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>
    </listeners>
    <test name="Send consolidated split-thread left-filter emails" preserve-order="true">
        <classes>
            <class name="com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterCombinedEmailReportTest">
                <methods><include name="sendCombinedPassedOrdersEmail"/></methods>
            </class>
        </classes>
    </test>
</suite>
"""
    COMBINED_SUITE.write_text(content, encoding="utf-8")


def main() -> None:
    runs = load_history_runs()
    all_filters = list_filter_dirs()

    thread1: list[dict] = []
    thread2: list[dict] = []
    total_tests = 0

    for filter_dir in all_filters:
        suite_path, test_count = build_filter_session_suite(filter_dir)
        rel_suite = suite_path.relative_to(PROJECT_ROOT).as_posix()
        entry = {
            "filterDir": filter_dir,
            "filterLabel": FILTER_LABELS.get(filter_dir, filter_dir),
            "suiteFile": rel_suite,
            "suiteBaseName": suite_path.stem,
            "testCount": test_count,
        }
        total_tests += test_count
        if is_filter_executed(filter_dir, runs):
            thread1.append(entry)
        else:
            thread2.append(entry)

    build_combined_email_suite()

    manifest = {
        "generatedAt": __import__("datetime").datetime.now().isoformat(timespec="seconds"),
        "historySource": str(HISTORY_JSON.relative_to(PROJECT_ROOT)).replace("\\", "/"),
        "aggregateDir": AGGREGATE_DIR,
        "combinedEmailSuite": str(COMBINED_SUITE.relative_to(PROJECT_ROOT)).replace("\\", "/"),
        "thread1ExecutedAtLeastOnce": thread1,
        "thread2NeverExecuted": thread2,
        "summary": {
            "totalFilters": len(all_filters),
            "thread1Filters": len(thread1),
            "thread2Filters": len(thread2),
            "totalTests": total_tests,
            "thread1Tests": sum(x["testCount"] for x in thread1),
            "thread2Tests": sum(x["testCount"] for x in thread2),
        },
    }

    MANIFEST_JSON.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST_JSON.write_text(json.dumps(manifest, indent=2), encoding="utf-8")

    print(f"Wrote manifest: {MANIFEST_JSON}")
    print(f"Thread 1 (executed before): {len(thread1)} filters, {manifest['summary']['thread1Tests']} tests")
    for item in thread1:
        print(f"  - {item['filterLabel']} ({item['testCount']} tests)")
    print(f"Thread 2 (never executed): {len(thread2)} filters, {manifest['summary']['thread2Tests']} tests")
    for item in thread2:
        print(f"  - {item['filterLabel']} ({item['testCount']} tests)")
    print(f"Combined email suite: {COMBINED_SUITE}")


if __name__ == "__main__":
    main()
