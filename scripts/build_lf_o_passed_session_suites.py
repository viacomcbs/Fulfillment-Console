#!/usr/bin/env python3
"""Generate PROD Synergy session suites for historically PASSED Orders left-filter tests only."""

from __future__ import annotations

import json
import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = (
    PROJECT_ROOT
    / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter/orders"
)
SESSIONS_DIR = PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/sessions"
HISTORY_JSON = PROJECT_ROOT / "docs/test-tracking/perfilter_execution_history.json"
COMBINED_SUITE = SESSIONS_DIR / "LF_O_Passed_CombinedEmail_ProdServerSuite.xml"

CORE8_FILTERS = [
    ("S01", "activitytype", "Activity Type"),
    ("S02", "assignedto", "Assigned To"),
    ("S03", "brand", "Brand"),
    ("S04", "environment", "Environment"),
    ("S05", "flag", "Flag"),
    ("S06", "lineitemstatus", "Line Item Status"),
    ("S07", "orderstatus", "Order Status"),
    ("S08", "submittedby", "Submitted By"),
]

METHOD_RE = re.compile(r"public\s+void\s+(\w+)\s*\(", re.MULTILINE)


def extract_test_method(java_file: Path) -> str:
    text = java_file.read_text(encoding="utf-8")
    if "@Test" not in text:
        return ""
    match = METHOD_RE.search(text)
    return match.group(1) if match else ""

SUITE_TITLE = "Orders Tab — Left Filters Regression (PROD)"
REPORT_EMAILS = (
    "Akilandeswari.Sundararajan@paramount.com,"
    "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
)
INTERNAL_REPORT_EMAIL = "Akilandeswari.Sundararajan@paramount.com"
SLACK_CHANNEL = "#qa_automation_status_go"


def load_passed_classes() -> set[str]:
    data = json.loads(HISTORY_JSON.read_text(encoding="utf-8"))
    latest_any: dict[str, dict] = {}
    latest_prod: dict[str, dict] = {}
    for row in data.get("runs", []):
        cls = row.get("testClass", "")
        if not cls.startswith("LF_O_TC"):
            continue
        ts = row.get("timestamp") or ""
        env = (row.get("environment") or "").upper()
        if cls not in latest_any or ts > (latest_any[cls].get("timestamp") or ""):
            latest_any[cls] = row
        if env == "PROD" and (cls not in latest_prod or ts > (latest_prod[cls].get("timestamp") or "")):
            latest_prod[cls] = row
    passed: set[str] = set()
    for cls in latest_any:
        row = latest_prod.get(cls, latest_any[cls])
        if row.get("status") == "Pass":
            passed.add(cls)
    return passed


def discover_filter_tests(filter_dir: str) -> list[tuple[str, str]]:
    folder = JAVA_ROOT / filter_dir
    entries: list[tuple[str, str]] = []
    for java_file in sorted(folder.glob("LF_*.java")):
        method = extract_test_method(java_file)
        if not method:
            continue
        entries.append((java_file.stem, method))
    entries.sort()
    return entries


def session_header(session_id: str, filter_label: str, maven_path: str, suite_name: str) -> str:
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Orders left-filter session on PROD Synergy (results merged into one report after all sessions).
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


def build_session_suite(session_id: str, filter_dir: str, filter_label: str, passed: set[str]) -> Path:
    file_name = f"LF_O_Passed_{session_id}_{filter_dir.title().replace(' ', '')}_ProdServerSuite.xml"
    out_path = SESSIONS_DIR / file_name
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    suite_name = f"LF Orders Passed {session_id} {filter_label} PROD"
    tests = [(cls, method) for cls, method in discover_filter_tests(filter_dir) if cls in passed]
    lines = [session_header(session_id, filter_label, maven_path, suite_name)]
    lines.append(f'    <test verbose="1" name="{filter_label} — left filters" preserve-order="true">')
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
    print(f"Wrote {out_path} ({len(tests)} passed tests)")
    return out_path


def build_combined_email_suite(session_count: int) -> None:
    maven_path = COMBINED_SUITE.relative_to(PROJECT_ROOT).as_posix()
    content = f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- One consolidated email + Slack after run-orders-left-filter-passed-prod.ps1 -->
<suite name="LF Orders Passed Combined Email PROD" parallel="false" preserve-order="true">
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SendReportEmailAddress" value="{REPORT_EMAILS}"/>
    <parameter name="SendReportInternalEmailAddress" value="{INTERNAL_REPORT_EMAIL}"/>
    <parameter name="SendChatReport" value="true"/>
    <parameter name="SlackChannel" value="{SLACK_CHANNEL}"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{SUITE_TITLE}"/>
    <parameter name="AggregateResultsDir" value="test-output/lf-o-passed-aggregate"/>
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
    <test name="Send consolidated passed left-filter email" preserve-order="true">
        <classes>
            <class name="com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterCombinedEmailReportTest">
                <methods><include name="sendCombinedPassedOrdersEmail"/></methods>
            </class>
        </classes>
    </test>
</suite>
"""
    COMBINED_SUITE.write_text(content, encoding="utf-8")
    print(f"Wrote {COMBINED_SUITE} (expects {session_count} session result files)")


def main() -> None:
    passed = load_passed_classes()
    session_paths: list[Path] = []
    total = 0
    for session_id, filter_dir, filter_label in CORE8_FILTERS:
        path = build_session_suite(session_id, filter_dir, filter_label, passed)
        session_paths.append(path)
        text = path.read_text(encoding="utf-8")
        total += text.count("<class name=")
    build_combined_email_suite(len(session_paths))
    print(f"Total passed tests scheduled: {total}")


if __name__ == "__main__":
    main()
