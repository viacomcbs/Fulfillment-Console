# Automation Count on `master` (for leadership)

**Last updated:** 2026-10-01  
**Branch:** `master` — passed-only core automation

## Report this number: **346** automated tests

| Bucket | Count | Passed at least once? |
|--------|------:|:---------------------:|
| Left filters (LF per-filter) | **182** | Yes (execution history) |
| Line Items — columns + sort | **134** | Yes (Babloo passed export) |
| In-sprint BSD stories | **30** | Yes (insprint suites) |
| **Total `@Test` on master** | **346** | |

Leadership slide deck rounds **182 + 134 + 45 = 361** (in-sprint **45** is a reporting bucket with story overlap; Git has **30** unique in-sprint test classes outside LF/LI).

## Not on `master` (removed from count)

- Orders table view columns / sort / search (~239)
- LI search (46)
- Duplicate filter options check / DFOC (46)
- FF_LD feature-flag cleanup (32)
- BSD-29174 / 29302 / 29791 / 30034 regression suites
- Never-passed / not-in-history tests

## Full suite backup (local only)

See [BACKUP_FULL_AUTOMATION.md](BACKUP_FULL_AUTOMATION.md).

## Verify count locally

```powershell
python -c "import re; from pathlib import Path; r=Path('src/test/java'); print(sum(len(re.findall(r'@Test\b', p.read_text(encoding='utf-8'))) for p in r.rglob('*.java') if '@Test' in p.read_text(encoding='utf-8')))"
```

Expected output: **346**
