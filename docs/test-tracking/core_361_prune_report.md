# Core 361 Prune Report

Generated after `scripts/prune_to_core_361_tests.py`.

## Kept `@Test` count: **346**

| Bucket | Count |
|--------|------:|
| LF per-filter (`LF_O_*` / `LF_LI_*`) | **182** |
| LI columns + sort (`TC_LI_*`) | **134** |
| In-sprint BSD (insprint XML, excl. FF_LD) | **30** |
| **Total in Git** | **346** |

Leadership **361** = 182 + 134 + **45** in-sprint. The repo has **30** unique in-sprint test classes outside LF/LI (TR, DSID, PTS, BSD-29967, BSD-30019). The KT **45** figure includes story-level reporting overlap; email-only runners were removed.

## Removed (summary)

- Orders table view: columns, sort, search (~239)
- LI search (46)
- DFOC duplicate-filter checks (46)
- FF_LD feature-flag cleanup (32)
- BSD-29174 / 29302 / 29791 / 30034 suites
- Misc legacy tests (`FilterPanelTests`, `ValidateHomePage`, table-view TC_00x, probes, etc.)

## Kept (non-counting)

- Helpers, page objects, base classes, listeners, utilities
- `fileproperties/` (`ConfigurationUtility`, `ExcelUtility`)
- LF base tests (`LeftFilter*BaseTest`, session helpers)
- `insprint-automation/` suite XMLs
- `regression/left-filters/` suite XMLs
- `LineItem_Passed_Columns_And_Sort_ProdServerSuite.xml`

## Verify

```bash
mvn test-compile
python -c "import re; from pathlib import Path; r=Path('src/test/java'); print(sum(len(re.findall(r'@Test', p.read_text())) for p in r.rglob('*.java') if '@Test' in p.read_text()))"
```
