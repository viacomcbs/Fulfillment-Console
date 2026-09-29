#!/usr/bin/env python3
"""Generate one PROD Synergy session suite XML per Line Items left-filter folder."""

from __future__ import annotations

import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "scripts"))
from generate_regression_suite_xml import discover_tests  # noqa: E402

JAVA_LINE_ITEMS = (
    PROJECT_ROOT
    / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter/lineitems"
)
SESSIONS_DIR = PROJECT_ROOT / "src/test/resources/regression/left-filters/line-items-view/sessions"

SKIP_DIRS = {"aggregated", "jobtype"}
REPORT_EMAIL = "Akilandeswari.Sundararajan@paramount.com"
SUITE_TITLE_PREFIX = "Line Items Tab - Left Filters (PROD)"

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


def list_filter_dirs() -> list[str]:
    return sorted(
        d.name
        for d in JAVA_LINE_ITEMS.iterdir()
        if d.is_dir() and d.name not in SKIP_DIRS
    )


def session_header(filter_dir: str, filter_label: str, maven_path: str, suite_name: str) -> str:
    suite_title = f"{SUITE_TITLE_PREFIX} - {filter_label}"
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Line Items left-filter session on PROD Synergy — one filter, one browser session.
     Filter: {filter_label}. Maven: mvn test "-DsuiteXmlFile={maven_path}" -->
<suite name="{suite_name}" parallel="false" preserve-order="true">

    <parameter name="RunAsFactory" value="false"/>
    <parameter name="Browser" value="Chrome"/>
    <parameter name="OS" value="Windows"/>
    <parameter name="OSVersion" value="11"/>
    <parameter name="DeviceCategory" value="Desktop"/>
    <parameter name="TestType" value="Regression"/>
    <parameter name="GuiType" value="Web"/>
    <parameter name="SendReportAutoEmails" value="true"/>
    <parameter name="SendReportEmailAddress" value="{REPORT_EMAIL}"/>
    <parameter name="SendChatReport" value="false"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{suite_title}"/>

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
    <parameter name="PathToReports" value="/test-output/reports/"/>
    <parameter name="PathToAllureResults" value="/Testreports/"/>
    <parameter name="PageLoadWaitTime" value="120"/>
    <parameter name="sendExecutionReport" value="true"/>
    <parameter name="authenticationReqd" value="true"/>
    <parameter name="recipientAddress" value="{REPORT_EMAIL}"/>
    <parameter name="senderAddress" value="mqe.automation@viacomcontractor.com"/>
    <parameter name="EmailRelayHost" value="imailrelay.viacom.com"/>
    <parameter name="EmailRelayPort" value="25"/>
    <parameter name="EmailSenderAddress" value="NOREPLY@dteqa.com"/>

    <listeners>
        <listener class-name="com.paramount.test.ff.common.listeners.AllureListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>
    </listeners>
"""


def build_filter_session_suite(filter_dir: str) -> tuple[Path, int]:
    filter_label = FILTER_LABELS.get(filter_dir, filter_dir.replace("_", " ").title())
    file_name = f"LF_LI_Split_{filter_dir}_ProdServerSuite.xml"
    out_path = SESSIONS_DIR / file_name
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    suite_name = f"LF Line Items Split {filter_label} PROD"

    tests = [
        (cls, method)
        for folder, cls, method in discover_tests("lineitems", [filter_dir])
        if folder == filter_dir
    ]
    if not tests:
        raise RuntimeError(f"No LF_LI tests found for filter folder: {filter_dir}")

    lines = [session_header(filter_dir, filter_label, maven_path, suite_name)]
    lines.append(f'    <test verbose="1" name="{filter_label} - left filters" preserve-order="true">')
    lines.append("        <classes>")
    for class_name, method_name in tests:
        fqcn = (
            f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems."
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


def main() -> None:
    total_tests = 0
    written: list[tuple[str, str, int]] = []
    for filter_dir in list_filter_dirs():
        out_path, test_count = build_filter_session_suite(filter_dir)
        rel = out_path.relative_to(PROJECT_ROOT).as_posix()
        label = FILTER_LABELS.get(filter_dir, filter_dir)
        written.append((filter_dir, rel, test_count))
        total_tests += test_count
        print(f"Wrote {rel} ({test_count} tests) — {label}")

    print(f"\nTotal: {len(written)} filters, {total_tests} tests")
    print(f"Output dir: {SESSIONS_DIR.relative_to(PROJECT_ROOT).as_posix()}")
    print("\nRun one filter (example):")
    print("  .\\scripts\\run-lf-lineitems-filter-prod.ps1 -Filter flag")


if __name__ == "__main__":
    main()
