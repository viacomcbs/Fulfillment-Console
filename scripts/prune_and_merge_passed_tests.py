#!/usr/bin/env python3
"""
Prune never-passed LF per-filter and LI columns/sort tests; merge Babloo exports.

Keep rules:
  - LF per-filter (perfilter/): merged execution history (Pass at least once) OR Babloo LF file
  - lineitem/columns + lineitem/sort: keep only files present in Babloo LI export
  - BSD / DSID: keep all
"""
from __future__ import annotations

import argparse
import json
import shutil
import sys
from pathlib import Path

LF_PREFIXES = ("LF_O_", "LF_LI_")
LF_PERFILTER = "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter"
LI_SUBDIRS = ("lineitem/columns", "lineitem/sort")


def load_json(path: Path) -> dict:
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def merge_histories(akila: dict, babloo: dict) -> dict:
    akila_runs = list(akila.get("runs", []))
    babloo_runs = list(babloo.get("runs", []))
    seen = {
        (
            r.get("testClass"),
            r.get("testMethod"),
            r.get("timestamp"),
            r.get("status"),
        )
        for r in akila_runs
    }
    for r in babloo_runs:
        sig = (
            r.get("testClass"),
            r.get("testMethod"),
            r.get("timestamp"),
            r.get("status"),
        )
        if sig not in seen:
            akila_runs.append(r)
            seen.add(sig)
    return {"runs": akila_runs}


def ever_passed(history: dict) -> set[str]:
    passed: set[str] = set()
    for row in history.get("runs", []):
        if row.get("status") == "Pass":
            cls = row.get("testClass", "")
            if cls:
                passed.add(f"{cls}.java")
    return passed


def collect_lf_test_files(root: Path) -> list[Path]:
    perfilter = root / LF_PERFILTER
    if not perfilter.is_dir():
        return []
    out: list[Path] = []
    for p in perfilter.rglob("*Test.java"):
        if any(p.name.startswith(pref) for pref in LF_PREFIXES):
            out.append(p)
    return out


def collect_li_test_files(root: Path, subdir: str) -> list[Path]:
    base = root / "src/test/java/com/paramount/test/ff/uitests/tests" / subdir
    if not base.is_dir():
        return []
    return list(base.glob("TC_LI_*.java"))


def babloo_li_keep_names(babloo_root: Path) -> dict[str, set[str]]:
    keep: dict[str, set[str]] = {}
    for sub in LI_SUBDIRS:
        files = collect_li_test_files(babloo_root, sub)
        keep[sub] = {p.name for p in files}
    return keep


def babloo_lf_keep_names(babloo_root: Path) -> set[str]:
    return {p.name for p in collect_lf_test_files(babloo_root)}


def prune_lf_tests(root: Path, keep_names: set[str]) -> tuple[list[str], list[str]]:
    deleted: list[str] = []
    kept: list[str] = []
    for p in collect_lf_test_files(root):
        if p.name in keep_names:
            kept.append(p.name)
        else:
            p.unlink()
            deleted.append(p.name)
    return kept, deleted


def prune_li_tests(root: Path, keep_by_dir: dict[str, set[str]]) -> tuple[list[str], list[str]]:
    deleted: list[str] = []
    kept: list[str] = []
    for sub, names in keep_by_dir.items():
        for p in collect_li_test_files(root, sub):
            if p.name in names:
                kept.append(p.name)
            else:
                p.unlink()
                deleted.append(p.name)
    return kept, deleted


def copy_babloo_li_resources(babloo_root: Path, merged_root: Path) -> list[str]:
    copied: list[str] = []
    rel_files = [
        "src/test/resources/regression/table-view/LineItem_Passed_Columns_And_Sort_ProdServerSuite.xml",
        "src/test/resources/regression/table-view/LineItemTabColumnsSuiteConfig.xml",
    ]
    for rel in rel_files:
        src = babloo_root / rel
        dst = merged_root / rel
        if src.is_file():
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)
            copied.append(rel)
    return copied


def clean_suite_xmls(resources_root: Path, deleted_classes: set[str]) -> int:
    if not deleted_classes:
        return 0
    updated = 0
    for xml in resources_root.rglob("*.xml"):
        text = xml.read_text(encoding="utf-8")
        original = text
        for cls in deleted_classes:
            simple = cls.replace(".java", "")
            # Remove full class element lines
            for pattern in (
                f'<class name="com.paramount.test.ff.uitests.tests.{simple}"/>',
                f'<class name="com.paramount.test.ff.uitests.tests.perfilter.{simple}"/>',
                f'<class name="com.paramount.test.ff.uitests.tests.lineitem.columns.{simple}"/>',
                f'<class name="com.paramount.test.ff.uitests.tests.lineitem.sort.{simple}"/>',
            ):
                text = text.replace(f"            {pattern}\n", "")
                text = text.replace(f"        {pattern}\n", "")
                text = text.replace(f"{pattern}\n", "")
            # FQCN variants
            fqcn_patterns = [
                f'com.paramount.test.ff.uitests.tests.perfilter.{simple}',
                f'com.paramount.test.ff.uitests.tests.lineitem.columns.{simple}',
                f'com.paramount.test.ff.uitests.tests.lineitem.sort.{simple}',
            ]
            for fqcn in fqcn_patterns:
                line = f'<class name="{fqcn}"/>'
                text = text.replace(f"            {line}\n", "")
                text = text.replace(f"        {line}\n", "")
                text = text.replace(f"{line}\n", "")
        if text != original:
            xml.write_text(text, encoding="utf-8")
            updated += 1
    return updated


def write_report(
    report_path: Path,
    *,
    lf_kept: list[str],
    lf_deleted: list[str],
    li_kept: list[str],
    li_deleted: list[str],
    copied: list[str],
    xml_updated: int,
) -> None:
    lines = [
        "# Passed Tests Merge Report",
        "",
        "## LF per-filter",
        f"- Kept: {len(lf_kept)}",
        f"- Deleted: {len(lf_deleted)}",
        "",
        "## LI columns + sort (Babloo keep list)",
        f"- Kept: {len(li_kept)}",
        f"- Deleted: {len(li_deleted)}",
        "",
        "## Babloo LI resources copied",
    ]
    lines.extend(f"- `{c}`" for c in copied)
    lines.extend(["", f"## Suite XML files updated: {xml_updated}", ""])
    if lf_deleted:
        lines.append("### LF deleted (sample first 20)")
        lines.extend(f"- {n}" for n in sorted(lf_deleted)[:20])
        lines.append("")
    if li_deleted:
        lines.append("### LI deleted (sample first 20)")
        lines.extend(f"- {n}" for n in sorted(li_deleted)[:20])
        lines.append("")
    report_path.write_text("\n".join(lines), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Prune and merge passed tests")
    parser.add_argument("--source", required=True, help="Source Fulfillment-Console root")
    parser.add_argument("--dest", required=True, help="Output merged root")
    parser.add_argument("--babloo-lf", help="Babloo LF per-filter export root")
    parser.add_argument("--babloo-li", help="Babloo LI columns/sort export root")
    parser.add_argument("--history", default="docs/test-tracking/perfilter_execution_history.json")
    args = parser.parse_args()

    source = Path(args.source).resolve()
    dest = Path(args.dest).resolve()
    babloo_lf = Path(args.babloo_lf).resolve() if args.babloo_lf else None
    babloo_li = Path(args.babloo_li).resolve() if args.babloo_li else None

    if dest.exists():
        shutil.rmtree(dest, ignore_errors=True)
        if dest.exists():
            # Windows: allure-report attachments can block rmtree
            for child in dest.iterdir():
                if child.is_dir():
                    shutil.rmtree(child, ignore_errors=True)
                else:
                    child.unlink(missing_ok=True)
    shutil.copytree(
        source,
        dest,
        ignore=shutil.ignore_patterns(".git", "target", "node_modules", "__pycache__", "allure-report"),
    )

    history_path = dest / args.history
    akila_hist = load_json(history_path) if history_path.is_file() else {}
    babloo_hist: dict = {}
    if babloo_lf:
        bh = babloo_lf / args.history
        if bh.is_file():
            babloo_hist = load_json(bh)
    merged_hist = merge_histories(akila_hist, babloo_hist)
    save_json(history_path, merged_hist)

    lf_keep = ever_passed(merged_hist)
    if babloo_lf:
        lf_keep |= babloo_lf_keep_names(babloo_lf)

    lf_kept, lf_deleted = prune_lf_tests(dest, lf_keep)

    li_kept: list[str] = []
    li_deleted: list[str] = []
    if babloo_li:
        keep_by_dir = babloo_li_keep_names(babloo_li)
        li_kept, li_deleted = prune_li_tests(dest, keep_by_dir)
        copied = copy_babloo_li_resources(babloo_li, dest)
    else:
        copied = []

    deleted_classes = {n.replace(".java", "") for n in lf_deleted + li_deleted}
    xml_updated = clean_suite_xmls(dest / "src/test/resources", deleted_classes)

    write_report(
        dest / "passed_merge_report.md",
        lf_kept=lf_kept,
        lf_deleted=lf_deleted,
        li_kept=li_kept,
        li_deleted=li_deleted,
        copied=copied,
        xml_updated=xml_updated,
    )

    print(f"LF kept={len(lf_kept)} deleted={len(lf_deleted)}")
    print(f"LI kept={len(li_kept)} deleted={len(li_deleted)}")
    print(f"Suite XMLs updated={xml_updated}")
    print(f"Report: {dest / 'passed_merge_report.md'}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
