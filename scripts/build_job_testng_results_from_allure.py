#!/usr/bin/env python3
"""Build testng-results.xml for Job left-filter email from allure-results/*-result.json."""

from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

JOB_SUITE = "LF Orders Core8 S09 Job PROD"
JOB_TEST = "Job - left filters"
PACKAGE = "com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job"


def allure_status_to_testng(status: str) -> str:
    normalized = (status or "").strip().lower()
    if normalized in {"passed", "pass"}:
        return "PASS"
    if normalized in {"failed", "fail", "broken"}:
        return "FAIL"
    if normalized in {"skipped", "skip"}:
        return "SKIP"
    return "UNKNOWN"


def load_job_results(allure_dir: Path) -> list[dict]:
    rows: list[dict] = []
    for path in sorted(allure_dir.glob("*-result.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        labels = {item.get("name"): item.get("value") for item in data.get("labels", [])}
        if labels.get("parentSuite") != JOB_SUITE:
            continue
        test_class = labels.get("testClass") or ""
        if not test_class.startswith(PACKAGE):
            continue
        method_name = labels.get("testMethod") or data.get("name") or "unknown"
        if not method_name.startswith("tc"):
            continue
        status = allure_status_to_testng(data.get("status", ""))
        message = ""
        details = data.get("statusDetails") or {}
        if isinstance(details, dict):
            message = (details.get("message") or "").strip()
        rows.append(
            {
                "class_name": test_class,
                "method_name": method_name,
                "status": status,
                "message": message,
            }
        )
    rows.sort(key=lambda row: row["class_name"])
    return rows


def write_testng_results(rows: list[dict], output_file: Path) -> None:
    passed = sum(1 for row in rows if row["status"] == "PASS")
    failed = sum(1 for row in rows if row["status"] == "FAIL")
    skipped = sum(1 for row in rows if row["status"] == "SKIP")

    root = ET.Element(
        "testng-results",
        {
            "ignored": "0",
            "total": str(len(rows)),
            "passed": str(passed),
            "failed": str(failed),
            "skipped": str(skipped),
        },
    )
    suite = ET.SubElement(root, "suite", {"name": JOB_SUITE})
    test = ET.SubElement(suite, "test", {"name": JOB_TEST})

    classes: dict[str, list[dict]] = {}
    for row in rows:
        classes.setdefault(row["class_name"], []).append(row)

    for class_name, methods in classes.items():
        class_el = ET.SubElement(test, "class", {"name": class_name})
        for row in methods:
            attrs = {
                "signature": f"{row['method_name']}()",
                "name": row["method_name"],
                "status": row["status"],
            }
            method_el = ET.SubElement(class_el, "test-method", attrs)
            if row["message"]:
                exception_el = ET.SubElement(method_el, "exception", {"message": row["message"]})

    tree = ET.ElementTree(root)
    ET.indent(tree, space="  ")
    output_file.parent.mkdir(parents=True, exist_ok=True)
    tree.write(output_file, encoding="utf-8", xml_declaration=True)


def main() -> int:
    repo_root = Path(__file__).resolve().parent.parent
    allure_dir = repo_root / "allure-results"
    aggregate_dir = repo_root / "test-output" / "lf-o-job-aggregate"
    output_file = aggregate_dir / "LF_O_Core8_S09_Job_ProdServerSuite-testng-results.xml"

    if not allure_dir.is_dir():
        print(f"Missing allure dir: {allure_dir}", file=sys.stderr)
        return 1

    rows = load_job_results(allure_dir)
    if not rows:
        print(f"No Job suite results found under {allure_dir}", file=sys.stderr)
        return 1

    write_testng_results(rows, output_file)
    print(f"Wrote {len(rows)} Job scenario(s) to {output_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
