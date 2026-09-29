#!/usr/bin/env python3
"""Move story/defect suite XMLs into insprint-automation/ with standard PROD service account."""

from __future__ import annotations

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
RES = PROJECT_ROOT / "src/test/resources"
OUT = RES / "insprint-automation"

SVC_USER = "svc-msc_bsd_qa_svc@paramount.com"
SVC_PASS = "D7h3M3Bo9SH4vJowwCXku2737!"

MIGRATIONS = [
    ("FF_DSID_BSD29870_ProdServerSuite.xml", "BSD-29870_ProdServerSuite.xml"),
    ("FF_BSD29967_DevServerSuite.xml", "BSD-29967_ProdServerSuite.xml"),
    ("FF_LD_BSD28459_ProdServerSuite.xml", "BSD-28459_ProdServerSuite.xml"),
    ("FF_PTS_BSD29441_ProdServerSuite.xml", "BSD-29441_Orders_ProdServerSuite.xml"),
    ("FF_PTS_BSD29441_LineItems_ProdServerSuite.xml", "BSD-29441_LineItems_ProdServerSuite.xml"),
    ("TR_TableRefresh_All_ProdServerSuite.xml", "BSD-29582_TableRefresh_ProdServerSuite.xml"),
]

USER_PATTERNS = [
    re.compile(r'<parameter name="Username" value="[^"]*"/>'),
    re.compile(r'<parameter name="Password" value="[^"]*"/>'),
]


def normalize_prod_params(content: str, new_maven_name: str) -> str:
    content = USER_PATTERNS[0].sub(f'<parameter name="Username" value="{SVC_USER}"/>', content)
    content = USER_PATTERNS[1].sub(f'<parameter name="Password" value="{SVC_PASS}"/>', content)
    content = re.sub(
        r'<parameter name="TestEnvironment" value="[^"]*"/>',
        '<parameter name="TestEnvironment" value="PROD"/>',
        content,
        count=1,
    )
    if 'TargetUrlDEV' not in content:
        insert = (
            '\n    <parameter name="TargetUrlDEV" value="https://dev-operationsconsole.paramountmsc.com/fulfillment/"/>'
            '\n    <parameter name="TargetUrlUAT" value="https://uat-operationsconsole.paramountmsc.com/fulfillment/"/>'
        )
        content = content.replace(
            '<parameter name="TargetUrlPROD"',
            insert + '\n    <parameter name="TargetUrlPROD"',
            1,
        )
    content = re.sub(
        r'mvn test "-DsuiteXmlFile=src/test/resources/[^"]+"',
        f'mvn test "-DsuiteXmlFile=src/test/resources/insprint-automation/{new_maven_name}"',
        content,
    )
    return content


def build_bsd_30019() -> str:
    orders = RES / "regression/left-filters/orders-view/LF_O_All_LeftFilters_ProdServerSuite.xml"
    line_items = RES / "regression/left-filters/line-items-view/LF_LI_All_LeftFilters_ProdServerSuite.xml"
    blocks: list[str] = []
    for path in (orders, line_items):
        text = path.read_text(encoding="utf-8")
        in_err = False
        for line in text.splitlines():
            if ".errormessage." in line and "<class name=" in line:
                in_err = True
            if in_err:
                blocks.append(line)
                if "</class>" in line:
                    in_err = False
    header = orders.read_text(encoding="utf-8").split("<listeners>")[0]
    header = re.sub(
        r'<suite name="[^"]*"',
        '<suite name="BSD-30019 Error Message PROD"',
        header,
        count=1,
    )
    header = re.sub(
        r'<parameter name="LeftFilterEmailSuiteTitle" value="[^"]*"/>',
        '<parameter name="LeftFilterEmailSuiteTitle" value="BSD-30019 — Error message left filter (Orders + Line Items)"/>',
        header,
    )
    header = re.sub(
        r'<!--.*?Maven: mvn test "[^"]+"',
        f'<!-- BSD-30019 Error code / left filter validation.\n     Maven: mvn test "-DsuiteXmlFile=src/test/resources/insprint-automation/BSD-30019_ProdServerSuite.xml"',
        header,
        count=1,
        flags=re.DOTALL,
    )
    body = "\n".join(
        [
            "    <listeners>",
            '        <listener class-name="com.paramount.test.ff.common.listeners.AllureListeners"/>',
            '        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>',
            '        <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>',
            "    </listeners>",
            "",
            '    <test verbose="1" name="BSD-30019 — Error message filter" preserve-order="true">',
            "        <classes>",
            *blocks,
            "        </classes>",
            "    </test>",
            "</suite>",
            "",
        ]
    )
    return header + body


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for src_name, dest_name in MIGRATIONS:
        src = RES / src_name
        if not src.is_file():
            print(f"Skip missing {src_name}")
            continue
        content = normalize_prod_params(src.read_text(encoding="utf-8"), dest_name)
        (OUT / dest_name).write_text(content, encoding="utf-8")
        print(f"Wrote insprint-automation/{dest_name}")

    print("BSD-30019: use scripts/build_bsd30019_suites.py (4 session XMLs under insprint-automation/)")


if __name__ == "__main__":
    main()
