#!/usr/bin/env python3
"""Scan Jira issue changelogs for UAT/PROD QA Status updates."""
import json
import glob
import os
import sys
from datetime import datetime

AGENT_DIR = os.environ.get(
    "AGENT_TOOLS_DIR",
    r"C:\Users\10747125\.cursor\projects\c-FulfillmentConsole-Fulfillment-Console\agent-tools",
)
EXTRA_DIRS = sys.argv[1:] if len(sys.argv) > 1 else []

KEYS = [
    "ADX-4977", "ADX-5054", "BSD-27950", "BSD-28523", "BSD-28532", "BSD-28606",
    "BSD-28748", "BSD-28828", "BSD-28919", "BSD-29043", "BSD-29355", "BSD-29358",
    "BSD-29376", "BSD-29387", "BSD-29436", "BSD-29440", "BSD-29502", "BSD-29515",
    "BSD-29531", "BSD-29578", "BSD-29582", "BSD-29587", "BSD-29594", "BSD-29615",
    "BSD-29636", "BSD-29685", "BSD-29701", "BSD-29725", "BSD-29726", "BSD-29727",
    "BSD-29736", "BSD-29738", "BSD-29739", "BSD-29741", "BSD-29743", "BSD-29751",
    "BSD-29753", "BSD-29756", "BSD-29758", "BSD-29791", "BSD-29823", "BSD-29836",
    "BSD-29842", "BSD-29844", "BSD-29851", "BSD-29856", "BSD-29862", "BSD-29869",
    "BSD-29870", "BSD-29877", "BSD-29879", "BSD-29882", "BSD-29883", "BSD-29886",
    "BSD-29887", "BSD-29900", "BSD-29905", "BSD-29908", "BSD-29909", "BSD-29910",
    "BSD-29911", "BSD-29912", "BSD-29928", "BSD-29936", "BSD-29937", "BSD-29939",
    "BSD-29941", "BSD-29959", "BSD-29960", "BSD-29961", "BSD-29980", "BSD-29981",
]

ROSTER = {
    "Sundararajan, Akilandeswari", "Byneni, Alekya", "Kumar, Babloo", "Zurang, Shubham",
    "1, Bhawna", "Suryavanshi, KrishnaKumar", "J, Rahul", "Balasundaram, Ravi",
    "Pawar, Rucha", "Arumugam, Vishnupriya", "K, Dhamodharan", "Dutta, Anjan",
}
WEEK_START = datetime(2026, 8, 17).date()
WEEK_END = datetime(2026, 8, 21).date()

UAT_FIELDS = {"UAT QA Status", "customfield_16437"}
PROD_FIELDS = {"PROD QA Status", "customfield_16438"}


def in_week(created: str) -> bool:
    # Jira changelog timestamps include America/New_York offset (e.g. -0400)
    date_part = created[:10]
    d = datetime.strptime(date_part, "%Y-%m-%d").date()
    return WEEK_START <= d <= WEEK_END


def classify(field: str, to_string: str):
    env = None
    if field in UAT_FIELDS or field == "UAT QA Status":
        env = "UAT"
    elif field in PROD_FIELDS or field == "PROD QA Status":
        env = "PROD"
    else:
        return None

    if to_string == "Working":
        return env, "Verified in UAT" if env == "UAT" else "Verified in PROD"
    if to_string == "-Not Working":
        return env, "Sent to dev"
    return None


def load_issues():
    issues = {}
    dirs = [AGENT_DIR] + EXTRA_DIRS
    for d in dirs:
        for path in glob.glob(os.path.join(d, "*.txt")) + glob.glob(os.path.join(d, "*.json")):
            try:
                with open(path, encoding="utf-8") as f:
                    data = json.load(f)
            except Exception:
                continue
            objs = []
            if isinstance(data, dict):
                if "key" in data and "changelog" in data:
                    objs = [data]
                elif "issues" in data:
                    objs = [i for i in data["issues"] if "changelog" in i]
            for obj in objs:
                k = obj.get("key")
                if k in KEYS:
                    issues[k] = obj
    return issues


def process_issue(obj):
    key = obj["key"]
    fields = obj.get("fields", {})
    summary = fields.get("summary", "")
    status = (fields.get("status") or {}).get("name", "")
    components = [c.get("name") for c in (fields.get("components") or []) if c.get("name")]

    matches = []
    for hist in (obj.get("changelog") or {}).get("histories") or []:
        author = (hist.get("author") or {}).get("displayName", "")
        if author not in ROSTER:
            continue
        created = hist.get("created", "")
        if not in_week(created):
            continue
        for item in hist.get("items") or []:
            field = item.get("field") or item.get("fieldId") or ""
            field_id = item.get("fieldId") or ""
            to_string = item.get("toString") or ""
            is_uat = field in UAT_FIELDS or field_id == "customfield_16437"
            is_prod = field in PROD_FIELDS or field_id == "customfield_16438"
            if not (is_uat or is_prod):
                continue
            result = classify(field, to_string)
            if not result:
                continue
            env, action = result
            matches.append({
                "key": key,
                "summary": summary,
                "status": status,
                "component names": components,
                "author displayName": author,
                "action": action,
                "environment": env,
                "change date": created,
                "field": field if field else field_id,
                "toString value": to_string,
                "_sort": created,
            })
    if not matches:
        return None
    matches.sort(key=lambda x: x["_sort"])
    best = matches[-1]
    del best["_sort"]
    return best


def main():
    mode = os.environ.get("MODE", "scan")
    issues = load_issues()
    if mode == "missing":
        missing = [k for k in KEYS if k not in issues]
        print(json.dumps({"cached": len(issues), "missing": missing}))
        return

    results = []
    for key in KEYS:
        obj = issues.get(key)
        if not obj:
            continue
        row = process_issue(obj)
        if row:
            results.append(row)

    counts = {
        "uat_working": sum(1 for r in results if r["environment"] == "UAT" and r["toString value"] == "Working"),
        "prod_working": sum(1 for r in results if r["environment"] == "PROD" and r["toString value"] == "Working"),
        "uat_not_working": sum(1 for r in results if r["environment"] == "UAT" and r["toString value"] == "-Not Working"),
        "prod_not_working": sum(1 for r in results if r["environment"] == "PROD" and r["toString value"] == "-Not Working"),
    }
    print(json.dumps({"results": results, "counts": counts, "scanned": len(issues), "missing_keys": [k for k in KEYS if k not in issues]}, indent=2))


if __name__ == "__main__":
    main()
