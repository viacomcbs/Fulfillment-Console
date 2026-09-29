#!/usr/bin/env python3
"""Generate consolidated left-filter regression suite XML (Orders + Line Items views)."""

from __future__ import annotations

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = (
    PROJECT_ROOT
    / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter"
)
OUT_ORDERS_ALL = PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/LF_O_All_LeftFilters_ProdServerSuite.xml"
OUT_ORDERS_CORE8 = (
    PROJECT_ROOT / "src/test/resources/regression/left-filters/orders-view/LF_O_Core8_LeftFilters_ProdServerSuite.xml"
)
OUT_LINE_ITEMS = PROJECT_ROOT / "src/test/resources/regression/left-filters/line-items-view/LF_LI_All_LeftFilters_ProdServerSuite.xml"

REGRESSION_FILTERS_ALL = [
    "activitytype",
    "assignedto",
    "brand",
    "environment",
    "errormessage",
    "flag",
    "lineitemstatus",
    "orderstatus",
    "submittedby",
]

# Orders regression default for PROD GHA — excludes Error Message until data stabilizes.
REGRESSION_FILTERS_ORDERS_CORE8 = [
    "activitytype",
    "assignedto",
    "brand",
    "environment",
    "flag",
    "lineitemstatus",
    "orderstatus",
    "submittedby",
]

SKIP_DIRS = {"aggregated"}

METHOD_RE = re.compile(
    r"@Test[^)]*\)(?:\s*@\w+(?:\([^)]*\))?\s*)*public\s+void\s+(\w+)\s*\(",
    re.MULTILINE | re.DOTALL,
)


def discover_tests(view: str, filters: list[str]) -> list[tuple[str, str, str]]:
    """Return (filter, class_name, method_name) sorted by filter then class."""
    base = JAVA_ROOT / view
    entries: list[tuple[str, str, str, str]] = []
    for filter_dir in sorted(filters):
        folder = base / filter_dir
        if not folder.is_dir():
            continue
        for java_file in sorted(folder.glob("LF_*.java")):
            text = java_file.read_text(encoding="utf-8")
            methods = METHOD_RE.findall(text)
            if not methods:
                continue
            class_name = java_file.stem
            for method in methods:
                entries.append((filter_dir, class_name, method, f"{filter_dir}/{class_name}"))
    entries.sort(key=lambda e: (e[0], e[1], e[2]))
    return [(e[0], e[1], e[2]) for e in entries]


def suite_header(
    view_label: str,
    suite_title: str,
    maven_path: str,
    filters: list[str],
    suite_name: str,
    send_chat_report: bool,
) -> str:
    chat_flag = "true" if send_chat_report else "false"
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- Consolidated {view_label} left-filter regression on PROD Synergy (sequential, shared session).
     Filters: {", ".join(filters)}.
     Maven: mvn test "-DsuiteXmlFile={maven_path}"
     IntelliJ: right-click this file -> Run -->
<suite name="{suite_name}" parallel="false" preserve-order="true">

    <parameter name="RunAsFactory" value="false"/>
    <parameter name="Browser" value="Chrome"/>
    <parameter name="OS" value="Windows"/>
    <parameter name="OSVersion" value="11"/>
    <parameter name="DeviceCategory" value="Desktop"/>
    <parameter name="TestType" value="Regression"/>
    <parameter name="GuiType" value="Web"/>
    <parameter name="SendReportAutoEmails" value="true"/>
    <parameter name="SendReportEmailAddress" value="Akilandeswari.Sundararajan@paramount.com"/>
    <parameter name="SendChatReport" value="{chat_flag}"/>
    <parameter name="SlackChannel" value="#fulfillment-console-qa"/>

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
    <parameter name="recipientAddress" value="Akilandeswari.Sundararajan@paramount.com"/>
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


def build_suite(
    view: str,
    view_label: str,
    suite_title: str,
    out_path: Path,
    filters: list[str],
    suite_name: str,
    send_chat_report: bool = False,
) -> None:
    pkg_view = view
    maven_path = out_path.relative_to(PROJECT_ROOT).as_posix()
    tests = discover_tests(view, filters)
    lines = [suite_header(view_label, suite_title, maven_path, filters, suite_name, send_chat_report)]
    lines.append(f'    <test verbose="1" name="{view_label} — left filters regression (PROD)" preserve-order="true">')
    lines.append("        <classes>")
    for filter_dir, class_name, method_name in tests:
        fqcn = f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.{pkg_view}.{filter_dir}.{class_name}"
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


def main() -> None:
    build_suite(
        "orders",
        "Orders",
        "Orders Tab — All left filters regression (PROD)",
        OUT_ORDERS_ALL,
        REGRESSION_FILTERS_ALL,
        "LF Orders All Left Filters PROD",
        send_chat_report=False,
    )
    build_suite(
        "orders",
        "Orders",
        "Orders Tab — Core 8 left filters regression (PROD)",
        OUT_ORDERS_CORE8,
        REGRESSION_FILTERS_ORDERS_CORE8,
        "LF Orders Core 8 Left Filters PROD",
        send_chat_report=True,
    )
    build_suite(
        "lineitems",
        "Line Items",
        "Line Items Tab — All left filters regression (PROD)",
        OUT_LINE_ITEMS,
        REGRESSION_FILTERS_ALL,
        "LF Line Items All Left Filters PROD",
        send_chat_report=False,
    )


if __name__ == "__main__":
    main()
