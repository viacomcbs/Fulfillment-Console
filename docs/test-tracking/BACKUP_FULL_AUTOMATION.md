# Full Automation Suite Backup

The **core passed-only** automation (~346 `@Test` methods) lives on **`master`**.

The **complete pre-prune suite** (~776 `@Test` methods) is preserved locally:

| Backup | Location |
|--------|----------|
| **Git branch** | `backup/full-automation-suite` (points to commit before core-361 prune) |
| **Local folder** | `C:\FulfillmentConsole\Fulfillment-Console-Full-Backup` (git worktree) |

## Restore a deleted test class

From repo root:

```powershell
git show backup/full-automation-suite:src/test/java/path/to/TestClass.java > src/test/java/path/to/TestClass.java
```

Or browse the worktree folder and copy files back.

## Restore entire full suite (emergency)

```powershell
git checkout backup/full-automation-suite -- src/test/java
```

Then reconcile with helpers — prefer restoring single files from the worktree copy instead.
