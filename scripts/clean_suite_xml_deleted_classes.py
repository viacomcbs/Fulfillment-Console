#!/usr/bin/env python3
"""Remove suite XML <class> blocks for deleted per-filter test classes."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
CLASS_BLOCK_RE = re.compile(r"\s*<class name=\"([^\"]+)\">.*?</class>\s*", re.DOTALL)


def load_deleted(merged_root: Path) -> set[str]:
    report = merged_root / "docs/test-tracking/passed_merge_report.md"
    if report.is_file():
        deleted: set[str] = set()
        in_section = False
        for line in report.read_text(encoding="utf-8").splitlines():
            if line.strip() == "## Deleted classes":
                in_section = True
                continue
            if in_section and line.startswith("- LF_"):
                deleted.add(line.strip()[2:])
        if deleted:
            return deleted

    perfilter = (
        PROJECT_ROOT
        / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter"
    )
    histories = [
        PROJECT_ROOT / "docs/test-tracking/perfilter_execution_history.json",
        Path(
            r"C:\Users\10747125\Downloads\Fulfillment-Console-PassedTests-2026-09-29"
            r"\Fulfillment-Console\docs\test-tracking\perfilter_execution_history.json"
        ),
    ]
    keep: set[str] = set()
    for path in histories:
        if not path.is_file():
            continue
        for row in json.loads(path.read_text(encoding="utf-8")).get("runs", []):
            if row.get("status") == "Pass":
                keep.add(row.get("testClass", ""))
    all_lf = {p.stem for p in perfilter.rglob("LF_*Test.java")}
    return all_lf - keep


def main() -> None:
    merged = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(
        r"C:\FulfillmentConsole\Fulfillment-Console-PassedMerged"
    )
    deleted = load_deleted(merged)
    print(f"Deleted class count for XML cleanup: {len(deleted)}")
    touched = 0
    for xml_file in (merged / "src/test/resources").rglob("*.xml"):
        text = xml_file.read_text(encoding="utf-8")
        if "LF_O_" not in text and "LF_LI_" not in text:
            continue
        original = text

        def replace_class(match: re.Match) -> str:
            simple = match.group(1).rsplit(".", 1)[-1]
            return "" if simple in deleted else match.group(0)

        text = CLASS_BLOCK_RE.sub(replace_class, text)
        if text != original:
            xml_file.write_text(text, encoding="utf-8")
            touched += 1
    print(f"Updated {touched} suite XML file(s)")


if __name__ == "__main__":
    main()
