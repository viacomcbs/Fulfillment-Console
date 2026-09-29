#!/usr/bin/env python3
"""Generate Batch-05 Orders left-filter suites: 5 filters in ONE Synergy session (calendar once)."""

from __future__ import annotations

import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "scripts"))
from generate_regression_suite_xml import discover_tests  # noqa: E402

SESSIONS_DIR = PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/sessions"
SUITE_TITLE = "Orders Tab - Left Filters Regression (PROD)"
STAKEHOLDER_EMAILS = (
    "Akilandeswari.Sundararajan@paramount.com,"
    "qa_automation_status_-aaaagmcoe366ao4pdnt2t5ikp4@viacomcbs.org.slack.com"
)
INTERNAL_EMAILS = "Akilandeswari.Sundararajan@paramount.com"
SLACK_CHANNEL = "#qa_automation_status_go"
AGGREGATE_DIR = "test-output/lf-o-batch05-aggregate"

BATCH05_SETS = {
    "Set01": [
        ("brand", "Brand"),
        ("flag", "Flag"),
        ("submittedby", "Submitted By"),
        ("lineitemstatus", "Line Item Status"),
        ("activitytype", "Activity Type"),
    ],
    "Set02": [
        ("orderstatus", "Order Status"),
        ("environment", "Environment"),
        ("assignedto", "Assigned To"),
        ("job", "Job type"),
        ("partner", "Partner"),
    ],
}

FILTER_LABELS = {key: label for filters in BATCH05_SETS.values() for key, label in filters}

# Same scenario order as LF_O_Core8_48_* session suites (Basic first, Clear last).
SCENARIO_ORDER = {
    "BasicTest": 0,
    "OptionOrderTest": 1,
    "SearchTest": 2,
    "SelectAllTest": 3,
    "ScrollTest": 4,
    "ActiveFiltersTest": 5,
    "TableSyncTest": 6,
    "ClearFiltersTest": 7,
}


def scenario_sort_key(class_name: str) -> tuple[int, str]:
    for suffix, order in SCENARIO_ORDER.items():
        if class_name.endswith(suffix):
            return (order, class_name)
    return (99, class_name)


def suite_header(set_id: str, filter_labels: list[str], maven_path: str) -> str:
    names = ", ".join(filter_labels)
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Batch 05 {set_id}: {names} — ONE Synergy login, Yesterday calendar once, shared browser session.
     Maven: mvn test "-DsuiteXmlFile={maven_path}" -->
<suite name="LF Orders Batch05 {set_id} PROD" parallel="false" preserve-order="true">

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
    <parameter name="LeftFilterBatchMode" value="true"/>
    <parameter name="AggregateResultsDir" value="{AGGREGATE_DIR}"/>

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


def build_batch_suite(set_id: str, filters: list[tuple[str, str]]) -> Path:
    filter_dirs = [f[0] for f in filters]
    all_tests = discover_tests("orders", filter_dirs)
    by_filter: dict[str, list[tuple[str, str]]] = {f[0]: [] for f in filters}
    for filter_dir, class_name, method_name in all_tests:
        by_filter.setdefault(filter_dir, []).append((class_name, method_name))

    file_name = f"LF_O_Batch05_{set_id}_ProdServerSuite.xml"
    out_path = SESSIONS_DIR / file_name
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    filter_labels = [label for _, label in filters]

    lines = [suite_header(set_id, filter_labels, maven_path)]
    total = 0
    for filter_dir, filter_label in filters:
        tests = sorted(by_filter.get(filter_dir, []), key=lambda t: scenario_sort_key(t[0]))
        total += len(tests)
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
    out_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {out_path} ({total} tests across {len(filters)} filters)")
    return out_path


def build_combined_email_suite(set_id: str) -> None:
    """Per-set consolidated email suite (history fallback matches suiteName in perfilter_execution_history.json)."""
    history_match = f"Batch05 {set_id}"
    out_path = SESSIONS_DIR / f"LF_O_Batch05_{set_id}_CombinedEmail_ProdServerSuite.xml"
    content = f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Consolidated email after run-lf-batch05-set-prod.ps1 -Set {set_id[-1]}. -->
<suite name="LF Orders Batch05 {set_id} Combined Email PROD" parallel="false" preserve-order="true">
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SkipSuiteFinishAutoEmail" value="true"/>
    <parameter name="SendReportEmailAddress" value="{STAKEHOLDER_EMAILS}"/>
    <parameter name="SendReportInternalEmailAddress" value="{INTERNAL_EMAILS}"/>
    <parameter name="SendChatReport" value="true"/>
    <parameter name="SlackChannel" value="{SLACK_CHANNEL}"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{SUITE_TITLE}"/>
    <parameter name="AggregateResultsDir" value="{AGGREGATE_DIR}"/>
    <parameter name="ExecutionHistorySuiteName" value="{history_match}"/>
    <parameter name="ConsolidatedEmailDedupePreferPass" value="true"/>
    <parameter name="ConsolidatedEmailExcludeSynergyIssues" value="true"/>
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
    <test name="Send consolidated Batch05 left-filter emails" preserve-order="true">
        <classes>
            <class name="com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterCombinedEmailReportTest">
                <methods><include name="sendCombinedPassedOrdersEmail"/></methods>
            </class>
        </classes>
    </test>
</suite>
"""
    out_path.write_text(content, encoding="utf-8")
    print(f"Wrote {out_path} (history match: {history_match})")


def main() -> None:
    grand_total = 0
    for set_id, filters in BATCH05_SETS.items():
        path = build_batch_suite(set_id, filters)
        grand_total += path.read_text(encoding="utf-8").count("<class name=")
        build_combined_email_suite(set_id)
    print(f"Batch05 total test classes: {grand_total}")


if __name__ == "__main__":
    main()
