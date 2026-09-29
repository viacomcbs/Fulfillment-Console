#!/usr/bin/env python3
"""Generate PROD Synergy session suites for ALL Core 8 Orders left-filter tests (split by filter)."""

from __future__ import annotations

import re
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "scripts"))
from generate_regression_suite_xml import REGRESSION_FILTERS_ORDERS_CORE8, discover_tests  # noqa: E402

SESSIONS_DIR = PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/sessions"
COMBINED_SUITE = SESSIONS_DIR / "LF_O_Core8_CombinedEmail_ProdServerSuite.xml"

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

SUITE_TITLE = "Orders Tab - Left Filters Regression (PROD)"
STAKEHOLDER_EMAILS = (
    "Akilandeswari.Sundararajan@paramount.com,"
    "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
)
INTERNAL_EMAILS = "Akilandeswari.Sundararajan@paramount.com"
SLACK_CHANNEL = "#qa_automation_status_go"
AGGREGATE_DIR = "test-output/lf-o-core8-aggregate"


def discover_filter_tests(filter_dir: str) -> list[tuple[str, str]]:
    rows = [(f, cls, method) for f, cls, method in discover_tests("orders", REGRESSION_FILTERS_ORDERS_CORE8) if f == filter_dir]
    rows.sort(key=lambda r: (r[1], r[2]))
    return [(cls, method) for _, cls, method in rows]


def session_header(session_id: str, filter_label: str, maven_path: str, suite_name: str) -> str:
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Core 8 Orders left-filter session on PROD Synergy (all scenarios for this filter).
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


def build_session_suite(session_id: str, filter_dir: str, filter_label: str) -> Path:
    file_name = f"LF_O_Core8_{session_id}_{filter_dir.title().replace(' ', '')}_ProdServerSuite.xml"
    out_path = SESSIONS_DIR / file_name
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    suite_name = f"LF Orders Core8 {session_id} {filter_label} PROD"
    tests = discover_filter_tests(filter_dir)
    lines = [session_header(session_id, filter_label, maven_path, suite_name)]
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
    print(f"Wrote {out_path} ({len(tests)} tests)")
    return out_path


def build_combined_email_suite(session_count: int) -> None:
    maven_path = COMBINED_SUITE.relative_to(PROJECT_ROOT).as_posix()
    content = f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Dual email after run-lf-only-prod.ps1: stakeholder passed-only + internal full status -->
<suite name="LF Orders Core8 Combined Email PROD" parallel="false" preserve-order="true">
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
    <test name="Send consolidated Core8 left-filter emails" preserve-order="true">
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
    total = 0
    session_count = 0
    for session_id, filter_dir, filter_label in CORE8_FILTERS:
        path = build_session_suite(session_id, filter_dir, filter_label)
        session_count += 1
        total += path.read_text(encoding="utf-8").count("<class name=")
    build_combined_email_suite(session_count)
    print(f"Total Core 8 tests scheduled: {total}")


if __name__ == "__main__":
    main()
