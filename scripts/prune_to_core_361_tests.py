#!/usr/bin/env python3
"""
Keep only core 361 @Test methods in Git:
  - 182 LF per-filter (LF_O_* / LF_LI_* under leftfiltersvalidation/perfilter)
  - 134 LI columns + sort (TC_LI_* under lineitem/columns|sort)
  - 45 in-sprint BSD (classes in insprint-automation/*.xml, excluding FF_LD / BSD-28459)

Non-@Test helpers, base classes, page objects, listeners, and utilities are kept.

Removes all other @Test classes and cleans empty directories.
"""
from __future__ import annotations

import re
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = PROJECT_ROOT / "src/test/java"
INSPRINT_DIR = PROJECT_ROOT / "src/test/resources/insprint-automation"
TABLE_VIEW_DIR = PROJECT_ROOT / "src/test/resources/regression/table-view"

SKIP_INSPRINT_SUITES = {"BSD-28459_ProdServerSuite.xml"}
SKIP_INSPRINT_CLASS_SUFFIXES = {
    "Bsd30019CombinedEmailReportTest",
    "LeftFilterCombinedEmailReportTest",
    "FulfillmentCombinedEmailReportTest",
}


def fqcn_to_path(fqcn: str) -> Path:
    return JAVA_ROOT / Path(*fqcn.split(".")).with_suffix(".java")


def load_insprint_keep_classes() -> set[str]:
    keep: set[str] = set()
    for xml in INSPRINT_DIR.glob("*.xml"):
        if xml.name in SKIP_INSPRINT_SUITES:
            continue
        root = ET.parse(xml).getroot()
        for cls in root.iter("class"):
            fqcn = cls.get("name", "")
            if not fqcn:
                continue
            simple = fqcn.split(".")[-1]
            if simple in SKIP_INSPRINT_CLASS_SUFFIXES:
                continue
            if simple.startswith("FF_LD_"):
                continue
            if simple.startswith("LF_"):
                continue  # counted in LF 182 bucket
            keep.add(fqcn)
    return keep


def is_lf_perfilter_test(path: Path) -> bool:
    rel = str(path.relative_to(JAVA_ROOT)).replace("\\", "/")
    return (
        "leftfiltersvalidation/perfilter" in rel
        and (path.name.startswith("LF_O_") or path.name.startswith("LF_LI_"))
    )


def is_li_col_sort_test(path: Path) -> bool:
    rel = str(path.relative_to(JAVA_ROOT)).replace("\\", "/")
    return (
        ("lineitem/columns/" in rel or "lineitem/sort/" in rel)
        and path.name.startswith("TC_LI_")
    )


def has_test_annotation(path: Path) -> bool:
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        return False
    return bool(re.search(r"@Test\b", text))


def is_support_file(path: Path) -> bool:
    rel = str(path.relative_to(JAVA_ROOT)).replace("\\", "/")
    if "duplicatefilteroptionscheck" in rel:
        return False
    if has_test_annotation(path):
        return False
    support_markers = (
        "/helpers/",
        "/pageobjects/",
        "/common/",
        "/uitests/base/",
        "/fileproperties/",
        "BaseTest.java",
        "Constants.java",
        "/testrail/",
        "/util/",
    )
    if any(m in rel for m in support_markers):
        return True
    if path.name.endswith("BaseTest.java"):
        return True
    if path.name.endswith("package-info.java"):
        return True
    # LF / insprint support under perfilter or tablevalidation without @Test
    if "leftfiltersvalidation/perfilter" in rel:
        return True
    if "tablevalidation/" in rel and not has_test_annotation(path):
        return True
    if "tablerefresh/" in rel and not has_test_annotation(path):
        return True
    if "ptspackaging/" in rel and not has_test_annotation(path):
        return True
    return False


def should_keep_java(path: Path, insprint_keep: set[str]) -> bool:
    if not path.suffix == ".java":
        return False
    if is_lf_perfilter_test(path):
        return True
    if is_li_col_sort_test(path):
        return True
    rel_pkg = ".".join(path.relative_to(JAVA_ROOT).with_suffix("").parts)
    if rel_pkg in insprint_keep:
        return True
    return is_support_file(path)


def prune_java_tests(insprint_keep: set[str]) -> tuple[list[Path], list[Path], int, int]:
    kept: list[Path] = []
    removed: list[Path] = []
    kept_tests = 0
    removed_tests = 0
    for path in sorted(JAVA_ROOT.rglob("*.java")):
        if should_keep_java(path, insprint_keep):
            kept.append(path)
            if has_test_annotation(path):
                kept_tests += len(re.findall(r"@Test\b", path.read_text(encoding="utf-8", errors="ignore")))
        else:
            removed.append(path)
            if has_test_annotation(path):
                removed_tests += len(re.findall(r"@Test\b", path.read_text(encoding="utf-8", errors="ignore")))
            path.unlink()
    return kept, removed, kept_tests, removed_tests


def prune_empty_dirs(root: Path) -> None:
    for path in sorted(root.rglob("*"), reverse=True):
        if not path.is_dir():
            continue
        try:
            next(path.iterdir())
        except StopIteration:
            try:
                path.rmdir()
            except OSError:
                pass


def prune_table_view_suites() -> list[str]:
    removed: list[str] = []
    if not TABLE_VIEW_DIR.is_dir():
        return removed
    keep = {
        "LineItem_Passed_Columns_And_Sort_ProdServerSuite.xml",
        "LineItemTabColumnsSuiteConfig.xml",
    }
    for xml in TABLE_VIEW_DIR.glob("*.xml"):
        if xml.name not in keep:
            xml.unlink()
            removed.append(xml.name)
    return removed


def main() -> int:
    insprint_keep = load_insprint_keep_classes()
    kept, removed, kept_tests, removed_tests = prune_java_tests(insprint_keep)
    prune_empty_dirs(JAVA_ROOT)
    removed_suites = prune_table_view_suites()

    report = PROJECT_ROOT / "docs/test-tracking/core_361_prune_report.md"
    report.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "# Core 361 Prune Report",
        "",
        f"- Kept `@Test` count: **{kept_tests}**",
        f"- Removed `@Test` count: **{removed_tests}**",
        f"- Kept Java files: {len(kept)}",
        f"- Removed Java files: {len(removed)}",
        f"- Insprint-only classes kept: {len(insprint_keep)}",
        f"- Removed table-view suite XMLs: {len(removed_suites)}",
        "",
        "## Target buckets",
        "- LF per-filter: 182",
        "- LI columns + sort: 134",
        "- In-sprint BSD: 45 (unique insprint-only in repo may differ if overlap)",
        "",
    ]
    if removed:
        lines.append("## Removed test files (sample first 40)")
        for p in removed[:40]:
            lines.append(f"- `{p.relative_to(PROJECT_ROOT).as_posix()}`")
        if len(removed) > 40:
            lines.append(f"- ... and {len(removed) - 40} more")
    report.write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(f"Kept @Test={kept_tests}  Removed @Test={removed_tests}")
    print(f"Removed Java files={len(removed)}")
    print(f"Report: {report}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
