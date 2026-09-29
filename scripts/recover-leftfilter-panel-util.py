#!/usr/bin/env python3
"""Recover LeftFilterPanelUtil.java by replaying StrReplace ops from agent transcripts."""
import json
import glob
import os
import sys

BASE = r"C:\Users\10747125\.cursor\projects\c-FulfillmentConsole-Fulfillment-Console\agent-transcripts"
TARGET = r"c:\FulfillmentConsole\Fulfillment-Console\src\test\java\com\paramount\test\ff\uitests\helpers\leftfilters\LeftFilterPanelUtil.java"

CHECK_METHODS = [
    "validateOrderLevelColumnTableSyncSmoke",
    "validateCountOnlyTableSyncSmoke",
    "validateActiveFiltersForPreservedSelection",
    "validateSearchAndSelectFirstOptionForFlow",
    "forceCollapseFilter",
    "resolveTableSyncOptionLabel",
]


def collect_transcripts():
    transcripts = []
    for fp in glob.glob(os.path.join(BASE, "**", "*.jsonl"), recursive=True):
        if "subagents" in fp.replace("\\", "/"):
            continue
        transcripts.append((os.path.getmtime(fp), fp))
    transcripts.sort()
    return transcripts


def apply_transcript(content, fp):
    applied = 0
    with open(fp, encoding="utf-8") as f:
        for line in f:
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                continue
            msg = obj.get("message", {})
            content_blocks = msg.get("content", [])
            if not isinstance(content_blocks, list):
                continue
            for block in content_blocks:
                if block.get("type") != "tool_use" or block.get("name") != "StrReplace":
                    continue
                inp = block.get("input", {})
                p = inp.get("path", "").replace("\\", "/")
                if not p.endswith("LeftFilterPanelUtil.java"):
                    continue
                old = inp.get("old_string", "")
                new = inp.get("new_string", "")
                if old and old in content:
                    content = content.replace(old, new, 1)
                    applied += 1
    return content, applied


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    with open(TARGET, encoding="utf-8") as f:
        content = f.read()

    total = 0
    for mtime, fp in collect_transcripts():
        content, applied = apply_transcript(content, fp)
        if applied:
            lines = content.count("\n") + 1
            print(f"{os.path.basename(fp)}: +{applied} -> {lines} lines")
            total += applied

    with open(TARGET, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)

    lines = content.count("\n") + 1
    print(f"TOTAL applied: {total}")
    print(f"Final lines: {lines}")
    print(f"Final bytes: {len(content.encode('utf-8'))}")
    for method in CHECK_METHODS:
        print(f"{method}: {'FOUND' if method in content else 'MISSING'}")


if __name__ == "__main__":
    main()
