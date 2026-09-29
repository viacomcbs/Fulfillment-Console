#!/usr/bin/env python3
"""Remove hardcoded Username/Password values from suite XMLs and generator scripts."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

USER_RE = re.compile(
    r'(<parameter\s+name="Username"\s+value=")[^"]*("\s*/>)',
    re.IGNORECASE,
)
PASS_RE = re.compile(
    r'(<parameter\s+name="Password"\s+value=")[^"]*("\s*/>)',
    re.IGNORECASE,
)
USER_RE_SP = re.compile(
    r'(<parameter\s+name="Username"\s+value=")[^"]*("\s+/>)',
    re.IGNORECASE,
)
PASS_RE_SP = re.compile(
    r'(<parameter\s+name="Password"\s+value=")[^"]*("\s+/>)',
    re.IGNORECASE,
)

# Generator script template literals
GEN_USER = re.compile(
    r'(<parameter name="Username" value=")[^"]*("/>)',
)
GEN_PASS = re.compile(
    r'(<parameter name="Password" value=")[^"]*("/>)',
)

# Commented credential blocks
COMMENTED_CREDS = re.compile(
    r"\s*<!--\s*<parameter name=\"Username\" value=\"[^\"]*\"/>.*?"
    r"<parameter name=\"Password\" value=\"[^\"]*\"/>.*?-->\s*\n",
    re.DOTALL,
)

SKIP_DIRS = {".git", "target", "node_modules", "__pycache__", "allure-report"}
EXTENSIONS = {".xml", ".py", ".java", ".properties", ".yml", ".yaml", ".json", ".md", ".ps1"}


def sanitize_text(text: str) -> tuple[str, bool]:
    original = text
    text = COMMENTED_CREDS.sub("\n", text)
    for pattern in (USER_RE, PASS_RE, USER_RE_SP, PASS_RE_SP, GEN_USER, GEN_PASS):
        text = pattern.sub(r"\1\2", text)
    return text, text != original


def main() -> int:
    changed = 0
    for path in ROOT.rglob("*"):
        if not path.is_file():
            continue
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        if path.suffix.lower() not in EXTENSIONS:
            continue
        if path.name == "sanitize_credentials.py":
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        new_text, did_change = sanitize_text(text)
        if did_change:
            path.write_text(new_text, encoding="utf-8")
            changed += 1
    print(f"Sanitized {changed} file(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
