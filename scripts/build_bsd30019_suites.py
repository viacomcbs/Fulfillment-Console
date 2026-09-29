#!/usr/bin/env python3
"""Build BSD-30019 session suite XMLs from the legacy combined suite."""

from pathlib import Path

PROJECT = Path(__file__).resolve().parents[1]
INS = PROJECT / "src/test/resources/insprint-automation"
ORDERS_SRC = INS / "BSD-30019_OrdersPerFilter_ProdServerSuite.xml"
LINE_ITEMS_SRC = INS / "BSD-30019_LineItemsPerFilter_ProdServerSuite.xml"

JIRA_KEY = "BSD-30019"
JIRA_STORY_TITLE = "Error code functionality mismatch between table and left filter"
EMAIL_SUITE_TITLE = f"{JIRA_KEY} — {JIRA_STORY_TITLE}"
TEST_ENVIRONMENT = "UAT"

COMMON_PARAMS = f"""
    <parameter name="RunAsFactory" value="false"/>
    <parameter name="Browser" value="Chrome"/>
    <parameter name="OS" value="Windows"/>
    <parameter name="OSVersion" value="11"/>
    <parameter name="DeviceCategory" value="Desktop"/>
    <parameter name="TestType" value="Regression"/>
    <parameter name="GuiType" value="Web"/>
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SendReportEmailAddress" value="Akilandeswari.Sundararajan@paramount.com"/>
    <parameter name="SendChatReport" value="false"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="Application" value="Fulfillment Console"/>
    <parameter name="ApplicationTitle" value="Fulfillment Console"/>
    <parameter name="TestEnvironment" value="{TEST_ENVIRONMENT}"/>
    <parameter name="LabUrl" value="https://www.synergyserver.tech"/>
    <parameter name="UserKey" value="af27f06e-2e7e-4c8d-9312-2320423e4641"/>
    <parameter name="LocalURL" value="http://localhost:39445/synergy"/>
    <parameter name="LocalExecution" value="false"/>
    <parameter name="ClientID" value="5CG23257SH"/>
    <parameter name="TargetUrlDEV" value="https://dev-operationsconsole.paramountmsc.com/fulfillment/"/>
    <parameter name="TargetUrlUAT" value="https://uat-operationsconsole.paramountmsc.com/fulfillment/"/>
    <parameter name="TargetUrlPROD" value="https://operationsconsole.paramountmsc.com/fulfillment/"/>
    <parameter name="Username" value=""/>
    <parameter name="Password" value=""/>
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
    <parameter name="JiraTicketID" value="{JIRA_KEY}"/>
    <parameter name="JiraStoryTitle" value="{JIRA_STORY_TITLE}"/>
"""

LISTENERS = """
    <listeners>
        <listener class-name="com.paramount.test.ff.common.listeners.AllureListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>
        <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>
    </listeners>
"""

STORY_CLASSES = [
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_O_001_ValidateErrorCodeCountSync", "validateErrorCodeCountSyncOrders"),
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_O_002_ValidateErrorCodeInTableColumn", "validateErrorCodeInTableColumnOrders"),
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_O_003_ValidateNoValueHasNoErrorCode", "validateNoValueHasNoErrorCodeOrders"),
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_LI_001_ValidateErrorCodeCountSync", "validateErrorCodeCountSyncLineItems"),
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_LI_002_ValidateErrorCodeInTableColumn", "validateErrorCodeInTableColumnLineItems"),
    ("com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.FF_BSD30019_LI_003_ValidateNoValueHasNoErrorCode", "validateNoValueHasNoErrorCodeLineItems"),
]


def extract_blocks(text: str, marker: str) -> list[str]:
    blocks = []
    capture = False
    buf: list[str] = []
    for line in text.splitlines():
        if marker in line and "<class name=" in line:
            capture = True
            buf = [line]
            continue
        if capture:
            buf.append(line)
            if "</class>" in line:
                blocks.append("\n".join(buf))
                capture = False
    return blocks


def build_suite(name: str, title: str, maven_file: str, test_name: str, class_blocks: list[str]) -> str:
    classes = "\n".join("            " + b.replace("\n", "\n            ") for b in class_blocks)
    return f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- In-sprint automation — {JIRA_KEY}: {JIRA_STORY_TITLE}
     {title}
     Maven: mvn test "-DsuiteXmlFile=src/test/resources/insprint-automation/{maven_file}" -->
<suite name="{name}" parallel="false" preserve-order="true">
{COMMON_PARAMS}
    <parameter name="LeftFilterEmailSuiteTitle" value="{title}"/>
{LISTENERS}
    <test verbose="1" name="{test_name}" preserve-order="true">
        <classes>
{classes}
        </classes>
    </test>
</suite>
"""


def main() -> None:
    if not ORDERS_SRC.exists() or not LINE_ITEMS_SRC.exists():
        raise SystemExit(
            "Missing BSD-30019 per-filter suite XMLs. "
            "Restore BSD-30019_OrdersPerFilter_ProdServerSuite.xml and "
            "BSD-30019_LineItemsPerFilter_ProdServerSuite.xml first."
        )
    orders = extract_blocks(ORDERS_SRC.read_text(encoding="utf-8"), ".orders.errormessage.")
    line_items = extract_blocks(LINE_ITEMS_SRC.read_text(encoding="utf-8"), ".lineitems.errormessage.")
    story = [
        f'            <class name="{fqcn}">\n                <methods><include name="{method}"/></methods>\n            </class>'
        for fqcn, method in STORY_CLASSES
    ]

    (INS / "BSD-30019_OrdersPerFilter_ProdServerSuite.xml").write_text(
        build_suite(
            "BSD-30019 Orders Per-Filter UAT",
            EMAIL_SUITE_TITLE,
            "BSD-30019_OrdersPerFilter_ProdServerSuite.xml",
            "Session 1 — Orders per-filter (8)",
            orders,
        ),
        encoding="utf-8",
    )
    (INS / "BSD-30019_LineItemsPerFilter_ProdServerSuite.xml").write_text(
        build_suite(
            "BSD-30019 Line Items Per-Filter UAT",
            EMAIL_SUITE_TITLE,
            "BSD-30019_LineItemsPerFilter_ProdServerSuite.xml",
            "Session 2 — Line Items per-filter (8)",
            line_items,
        ),
        encoding="utf-8",
    )
    (INS / "BSD-30019_StoryValidation_ProdServerSuite.xml").write_text(
        build_suite(
            "BSD-30019 Story Validation UAT",
            EMAIL_SUITE_TITLE,
            "BSD-30019_StoryValidation_ProdServerSuite.xml",
            "Session 3 — Story validation (6)",
            story,
        ),
        encoding="utf-8",
    )
    email = f"""<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<!-- In-sprint automation — {JIRA_KEY}: combined email only (after run-bsd-30019.ps1 / .sh). -->
<suite name="BSD-30019 Combined Email" parallel="false" preserve-order="true">
    <parameter name="SendReportAutoEmails" value="false"/>
    <parameter name="SendReportEmailAddress" value="Akilandeswari.Sundararajan@paramount.com"/>
    <parameter name="LeftFilterEmailReport" value="true"/>
    <parameter name="LeftFilterEmailSuiteTitle" value="{EMAIL_SUITE_TITLE}"/>
    <parameter name="AggregateResultsDir" value="test-output/bsd-30019-aggregate"/>
    <parameter name="Application" value="Fulfillment Console"/>
    <parameter name="ApplicationTitle" value="Fulfillment Console"/>
    <parameter name="TestEnvironment" value="{TEST_ENVIRONMENT}"/>
    <parameter name="LocalExecution" value="true"/>
    <parameter name="EmailRelayHost" value="imailrelay.viacom.com"/>
    <parameter name="EmailRelayPort" value="25"/>
    <parameter name="EmailSenderAddress" value="NOREPLY@dteqa.com"/>
    <listeners>
        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>
    </listeners>
    <test name="Send combined BSD-30019 email" preserve-order="true">
        <classes>
            <class name="com.paramount.test.ff.uitests.tests.tablevalidation.bsd30019.Bsd30019CombinedEmailReportTest">
                <methods><include name="sendCombinedBsd30019Email"/></methods>
            </class>
        </classes>
    </test>
</suite>
"""
    (INS / "BSD-30019_SendCombinedEmail_ProdServerSuite.xml").write_text(email, encoding="utf-8")
    print("Wrote 4 BSD-30019 session suite XMLs")


if __name__ == "__main__":
    main()
