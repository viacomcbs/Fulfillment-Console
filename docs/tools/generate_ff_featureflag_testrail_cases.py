#!/usr/bin/env python3
"""Generate TestRail CSV import files for BSD-28459 Feature Flag cleanup manual test cases."""
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "testrail"
REF = "BSD-28459"
SECTION_ROOT = f"Fulfillment Console > {REF} > Feature Flag Cleanup"
PKG = "com.paramount.test.ff.uitests.tests.featureflag.bsd28459"

HEADER = [
    "Title", "Section", "Template", "Type", "Priority", "References",
    "Automation Type", "Automation ID", "Preconditions", "Steps", "Expected Result",
]

PRE_BASE = "UAT or PROD Fulfillment Console build containing BSD-28459 feature-flag cleanup; valid Okta user with FF access"
PRE_ORDERS = PRE_BASE + "; Orders tab; calendar = Yesterday"
PRE_LINE_ITEMS = PRE_BASE + "; Line Items tab; calendar = Yesterday"


def auto_id(num: str, cls_suffix: str, method: str) -> str:
    return f"{PKG}.FF_LD_{num}_{cls_suffix}#{method}"


def row(num, title, subsection, priority, pre, steps, expected, cls_suffix, method, flag=""):
    ref = REF if not flag else f"{REF}; flag={flag}"
    return {
        "Title": f"FF_LD_{num} — {title}",
        "Section": f"{SECTION_ROOT} > {subsection}",
        "Template": "Test Case (Text)",
        "Type": "Regression",
        "Priority": priority,
        "References": ref,
        "Automation Type": "Java TestNG",
        "Automation ID": auto_id(num, cls_suffix, method),
        "Preconditions": pre,
        "Steps": steps,
        "Expected Result": expected,
    }


CASES = [
    row(
        "001", "App bootstrap after Okta login (enable-okta-auth removed)", "Authentication",
        "1 - Critical", PRE_BASE,
        "1. Open Fulfillment Console URL in a fresh browser session\n"
        "2. Complete Okta SSO login with a valid FF user\n"
        "3. Wait for the application shell to finish loading\n"
        "4. Open browser DevTools → Console tab\n"
        "5. Confirm Orders or Line Items tab is reachable",
        "App bootstraps successfully without blank screen or auth-guard loop; no JS errors related to removed enable-okta-auth flag",
        "ValidateAppBootstrapAfterOktaLogin", "validateAppBootstrapAfterOktaLogin", "enable-okta-auth",
    ),
    row(
        "002", "Okta token sent on API requests (add-okta-token-to-http-header)", "Authentication",
        "1 - Critical", PRE_BASE,
        "1. Log in to Fulfillment Console\n"
        "2. Open DevTools → Network tab; filter XHR/Fetch\n"
        "3. Navigate to Orders tab and wait for grid data to load\n"
        "4. Select any GraphQL or REST request to the FF backend\n"
        "5. Inspect Request Headers for Authorization or configured Okta token header",
        "Authenticated API requests include the Okta bearer/token header; requests succeed (HTTP 200) without 401 due to missing token",
        "ValidateOktaTokenInHttpHeaders", "validateOktaTokenInHttpHeaders", "add-okta-token-to-http-header",
    ),
    row(
        "003", "Orders view loads with dynamic GraphQL (enable-dynamic-graphql-generation-for-orders-view)", "Orders View",
        "1 - Critical", PRE_ORDERS,
        "1. Log in; set calendar to Yesterday\n"
        "2. Navigate to Orders tab\n"
        "3. Wait for grid to populate\n"
        "4. Open DevTools → Network; confirm GraphQL request for orders view\n"
        "5. Apply one left filter (e.g. Order Status → one value) and wait for refresh",
        "Orders grid loads with data; GraphQL query succeeds; filtered results refresh without page reload or console errors",
        "ValidateOrdersViewDynamicGraphql", "validateOrdersViewDynamicGraphql",
        "enable-dynamic-graphql-generation-for-orders-view",
    ),
    row(
        "004", "Material ID column available in Orders Table View (add-material-id-to-both-the-views)", "Orders View",
        "1 - Critical", PRE_ORDERS,
        "1. Log in; navigate to Orders tab\n"
        "2. Open Table View (Manage columns)\n"
        "3. Search or scroll for Material ID\n"
        "4. Enable Material ID checkbox\n"
        "5. Apply / Save changes and close Table View",
        "Material ID appears in Manage columns list and can be enabled on Orders grid",
        "ValidateMaterialIdAvailableInOrdersTableView", "validateMaterialIdAvailableInOrdersTableView",
        "add-material-id-to-both-the-views",
    ),
    row(
        "005", "Material ID column visible and populated in Orders grid", "Orders View",
        "1 - Critical", PRE_ORDERS + "; Material ID enabled in Table View",
        "1. On Orders tab with Material ID column enabled\n"
        "2. Horizontal scroll to Material ID column if needed\n"
        "3. Review column header text\n"
        "4. Review cell values on first 5 visible rows",
        "Material ID column header is visible; cells show values where backend data exists (not blank for all rows when data exists)",
        "ValidateMaterialIdVisibleInOrdersGrid", "validateMaterialIdVisibleInOrdersGrid",
        "add-material-id-to-both-the-views",
    ),
    row(
        "006", "Edit CRID on Orders row (add-edit-crid-to-both-the-views)", "Orders View",
        "2 - High", PRE_ORDERS + "; user has edit permission; CRID editable row available",
        "1. On Orders tab locate a row with editable CRID\n"
        "2. Open inline edit or edit action for CRID field\n"
        "3. Change CRID to a valid test value (or append suffix per test data policy)\n"
        "4. Save the change\n"
        "5. Refresh grid or reopen row and verify persisted value",
        "CRID edit UI opens; save succeeds; updated CRID persists in grid after refresh",
        "ValidateEditCridOnOrdersRow", "validateEditCridOnOrdersRow", "add-edit-crid-to-both-the-views",
    ),
    row(
        "007", "Partner End Date column visible in Orders grid (enable-partner-end-date-column)", "Orders View",
        "2 - High", PRE_ORDERS,
        "1. Log in; navigate to Orders tab\n"
        "2. Open Table View → enable Partner End Date if not already visible\n"
        "3. Apply changes and locate column in grid\n"
        "4. Compare displayed dates to known test order if available",
        "Partner End Date column is present in grid and displays date values in expected format",
        "ValidatePartnerEndDateColumnInOrders", "validatePartnerEndDateColumnInOrders",
        "enable-partner-end-date-column",
    ),
    row(
        "008", "UUID displayed in Orders view (enable-display-of-uuid)", "Orders View",
        "2 - High", PRE_ORDERS,
        "1. Log in; navigate to Orders tab\n"
        "2. Open Table View and enable UUID column if available\n"
        "3. Apply changes\n"
        "4. Locate UUID values in grid or open order Details panel\n"
        "5. Verify UUID format (standard UUID pattern)",
        "UUID is visible in Orders grid or Details where feature was previously flag-gated; values match UUID format",
        "ValidateUuidDisplayedInOrdersView", "validateUuidDisplayedInOrdersView", "enable-display-of-uuid",
    ),
    row(
        "009", "Order History v2 in order Details (enable-order-history-in-the-order-view-v2)", "Orders View",
        "2 - High", PRE_ORDERS + "; order with history exists",
        "1. On Orders tab select an order with known history\n"
        "2. Open order Details / side panel\n"
        "3. Navigate to Order History section or tab\n"
        "4. Wait for history entries to load\n"
        "5. Expand one history entry if collapsible",
        "Order History v2 panel loads; history entries display with expected fields; no fallback to deprecated v1-only layout",
        "ValidateOrderHistoryV2InOrderDetails", "validateOrderHistoryV2InOrderDetails",
        "enable-order-history-in-the-order-view-v2",
    ),
    row(
        "010", "Line Items view loads (enable-line-item-view)", "Line Items View",
        "1 - Critical", PRE_LINE_ITEMS,
        "1. Log in; set calendar to Yesterday\n"
        "2. Click Line Items tab\n"
        "3. Wait for grid to populate\n"
        "4. Verify left filter panel and grid headers render",
        "Line Items view loads without error; grid shows data; no blank page from removed flag branch",
        "ValidateLineItemsViewLoads", "validateLineItemsViewLoads", "enable-line-item-view",
    ),
    row(
        "011", "Line Items dynamic GraphQL (enable-dynamic-graphql-generation-for-li-view)", "Line Items View",
        "1 - Critical", PRE_LINE_ITEMS,
        "1. On Line Items tab wait for initial load\n"
        "2. Open DevTools → Network → filter GraphQL\n"
        "3. Apply Line Item Status filter with one option\n"
        "4. Confirm new GraphQL request and grid refresh",
        "Line Items GraphQL query succeeds after filter change; grid updates to match selection",
        "ValidateLineItemsDynamicGraphql", "validateLineItemsDynamicGraphql",
        "enable-dynamic-graphql-generation-for-li-view",
    ),
    row(
        "012", "Material ID column in Line Items grid (add-material-id-to-both-the-views)", "Line Items View",
        "2 - High", PRE_LINE_ITEMS,
        "1. Navigate to Line Items tab\n"
        "2. Open Table View → enable Material ID\n"
        "3. Apply changes\n"
        "4. Verify column header and sample cell values",
        "Material ID column visible on Line Items grid with populated values where data exists",
        "ValidateMaterialIdInLineItemsGrid", "validateMaterialIdInLineItemsGrid",
        "add-material-id-to-both-the-views",
    ),
    row(
        "013", "Edit CRID on Line Items row (add-edit-crid-to-both-the-views)", "Line Items View",
        "2 - High", PRE_LINE_ITEMS + "; editable CRID row available",
        "1. On Line Items tab locate editable CRID row\n"
        "2. Open CRID edit control\n"
        "3. Modify and save CRID\n"
        "4. Verify persisted value after grid refresh",
        "CRID edit works on Line Items view; saved value persists",
        "ValidateEditCridOnLineItemsRow", "validateEditCridOnLineItemsRow", "add-edit-crid-to-both-the-views",
    ),
    row(
        "014", "Open Text ID shown at line item level (show-open-text-id-on-the-line-item-level)", "Line Items View",
        "2 - High", PRE_LINE_ITEMS + "; line item with Open Text ID exists",
        "1. On Line Items tab filter or search for item with Open Text ID\n"
        "2. Enable Open Text ID column in Table View if needed\n"
        "3. Locate value at line item row level\n"
        "4. Open Details panel for same row and compare",
        "Open Text ID visible at line item level in grid and matches Details panel",
        "ValidateOpenTextIdOnLineItemLevel", "validateOpenTextIdOnLineItemLevel",
        "show-open-text-id-on-the-line-item-level",
    ),
    row(
        "015", "Live filter count updates in Line Items view (enable-live-updates-for-filterables-for-line-item-view)", "Line Items View",
        "3 - Medium", PRE_LINE_ITEMS + "; environment supports live updates",
        "1. On Line Items tab expand a filter with non-zero counts (e.g. Line Item Status)\n"
        "2. Note count on one option\n"
        "3. In a second session or via backend trigger, change data affecting that count (if test env supports)\n"
        "4. Observe filter option count without full page refresh",
        "Filter option counts update via live subscription/polling without manual page reload (or remain stable if no backend change)",
        "ValidateLiveFilterCountUpdatesLineItems", "validateLiveFilterCountUpdatesLineItems",
        "enable-live-updates-for-filterables-for-line-item-view",
    ),
    row(
        "016", "Live table row updates in Line Items view (enable-live-updates-for-table-in-the-line-item-view)", "Line Items View",
        "3 - Medium", PRE_LINE_ITEMS,
        "1. On Line Items tab load grid with several rows\n"
        "2. Note status/value on a visible row\n"
        "3. Trigger external update to that line item if test data allows\n"
        "4. Observe row update without full refresh",
        "Table row reflects live data changes without requiring full page reload",
        "ValidateLiveTableUpdatesLineItems", "validateLiveTableUpdatesLineItems",
        "enable-live-updates-for-table-in-the-line-item-view",
    ),
    row(
        "017", "Refactored left filter panel renders (use-refactored-filter-panel)", "Filter Panel",
        "1 - Critical", PRE_ORDERS,
        "1. Log in; navigate to Orders tab\n"
        "2. Locate left filter accordion panel\n"
        "3. Expand Order Status and Partner filters\n"
        "4. Repeat on Line Items tab with Line Item Status filter",
        "Refactored filter panel UI loads on both tabs; accordions expand; options render without layout break",
        "ValidateRefactoredFilterPanelRenders", "validateRefactoredFilterPanelRenders",
        "use-refactored-filter-panel",
    ),
    row(
        "018", "Filter category pagination works (enable-pagination-in-the-filter-categories)", "Filter Panel",
        "2 - High", PRE_ORDERS,
        "1. On Orders tab expand Partner filter (large option list)\n"
        "2. Locate pagination controls at bottom of filter options\n"
        "3. Click Next page\n"
        "4. Click Previous page\n"
        "5. Repeat on Line Items → Partner filter",
        "Pagination navigates additional option pages without error; selections persist when returning to prior page",
        "ValidateFilterCategoryPagination", "validateFilterCategoryPagination",
        "enable-pagination-in-the-filter-categories",
    ),
    row(
        "019", "Big filter counts updated behaviour (enable-big-filter-counts-updated-behaviour)", "Filter Panel",
        "2 - High", PRE_ORDERS + "; Yesterday calendar with substantial data volume",
        "1. On Orders tab expand multiple filters with large counts\n"
        "2. Select one filter option and note table total count\n"
        "3. Add a second filter selection\n"
        "4. Compare filter badge counts and table total",
        "Filter counts and table total stay consistent when applying multiple filters on large datasets; no stale zero counts",
        "ValidateBigFilterCountsBehaviour", "validateBigFilterCountsBehaviour",
        "enable-big-filter-counts-updated-behaviour",
    ),
    row(
        "020", "Enhanced date range filtering (enable-enhanced-date-range-filtering)", "Filter Panel",
        "2 - High", PRE_ORDERS,
        "1. On Orders tab open calendar / date range control\n"
        "2. Select a custom date range (e.g. last 7 days)\n"
        "3. Apply range and wait for grid refresh\n"
        "4. Verify rows fall within selected dates",
        "Enhanced date range filter applies correctly; grid shows orders within selected range only",
        "ValidateEnhancedDateRangeFiltering", "validateEnhancedDateRangeFiltering",
        "enable-enhanced-date-range-filtering",
    ),
    row(
        "021", "Date range customization presets (enable-enhanced-date-range-filtering-customization)", "Filter Panel",
        "3 - Medium", PRE_ORDERS,
        "1. Open date range picker on Orders tab\n"
        "2. Select a preset (e.g. Yesterday, Last 7 days, Custom)\n"
        "3. Switch between presets\n"
        "4. Apply and verify grid updates each time",
        "Date range presets and customization options work; grid refreshes per selection",
        "ValidateDateRangeCustomization", "validateDateRangeCustomization",
        "enable-enhanced-date-range-filtering-customization",
    ),
    row(
        "022", "History log in order view (enable-history-log-functionality)", "Order History & Audit",
        "2 - High", PRE_ORDERS + "; order with history",
        "1. Open an order with known history on Orders tab\n"
        "2. Navigate to History / History Log section\n"
        "3. Scroll through entries\n"
        "4. Verify timestamp, user, and action fields present",
        "History log entries load and display expected metadata",
        "ValidateHistoryLogFunctionality", "validateHistoryLogFunctionality",
        "enable-history-log-functionality",
    ),
    row(
        "023", "Audit log entries visible (enable-audit-log)", "Order History & Audit",
        "2 - High", PRE_ORDERS + "; order with audit events",
        "1. Open order Details for order with audit activity\n"
        "2. Open Audit Log section\n"
        "3. Review latest audit entries\n"
        "4. Expand detail on one entry if supported",
        "Audit log section renders entries with correct event types and timestamps",
        "ValidateAuditLogEntries", "validateAuditLogEntries", "enable-audit-log",
    ),
    row(
        "024", "Revision-wise audit log (order-history-show-revision-wise-audit-log)", "Order History & Audit",
        "2 - High", PRE_ORDERS + "; multi-revision order",
        "1. Open order with multiple revisions\n"
        "2. Navigate to Order History / Audit area\n"
        "3. Verify entries grouped or labeled by revision\n"
        "4. Compare revision identifiers across entries",
        "Audit/history entries are organized revision-wise as per enabled behaviour",
        "ValidateRevisionWiseAuditLog", "validateRevisionWiseAuditLog",
        "order-history-show-revision-wise-audit-log",
    ),
    row(
        "025", "Older line items differentiated in history (order-history-enable-differentiations-for-older-line-items)", "Order History & Audit",
        "3 - Medium", PRE_ORDERS + "; order with older line items",
        "1. Open order containing older and current line items\n"
        "2. Open Order History or line item history view\n"
        "3. Compare visual or label treatment of older vs current line items",
        "Older line items are visually or textually differentiated from current line items",
        "ValidateOlderLineItemDifferentiation", "validateOlderLineItemDifferentiation",
        "order-history-enable-differentiations-for-older-line-items",
    ),
    row(
        "026", "Revised statuses displayed (show-revised-statuses)", "Order History & Audit",
        "2 - High", PRE_ORDERS + "; order with revised status",
        "1. Locate order with revised status in Orders grid\n"
        "2. Review Status column value\n"
        "3. Open Details and confirm revised status label/format",
        "Revised statuses display in grid and Details with expected revised-status formatting",
        "ValidateRevisedStatusesDisplayed", "validateRevisedStatusesDisplayed", "show-revised-statuses",
    ),
    row(
        "027", "Unified history with approval processing (show-ops-console-unified-history-with-approval-processing)", "Order History & Audit",
        "2 - High", PRE_ORDERS + "; order with approval workflow history",
        "1. Open order with approval processing history\n"
        "2. Navigate to unified history / ops console history view\n"
        "3. Locate approval-related history entries\n"
        "4. Verify approval status transitions are listed",
        "Unified history shows approval processing events integrated with other history entries",
        "ValidateUnifiedHistoryWithApproval", "validateUnifiedHistoryWithApproval",
        "show-ops-console-unified-history-with-approval-processing",
    ),
    row(
        "028", "GLIM sidecar files load (enable-side-car-files-loading-in-glim)", "GLIM",
        "2 - High", PRE_LINE_ITEMS + "; asset with sidecar files",
        "1. On Line Items tab open row with GLIM / preview available\n"
        "2. Launch GLIM player for asset with sidecar files\n"
        "3. Open sidecar / associated files panel in GLIM\n"
        "4. Attempt to load each listed sidecar file",
        "Sidecar files list appears and each file loads without error in GLIM",
        "ValidateGlimSidecarFilesLoad", "validateGlimSidecarFilesLoad",
        "enable-side-car-files-loading-in-glim",
    ),
    row(
        "029", "GLIM Play All files (enable-play-all-files-in-glim)", "GLIM",
        "2 - High", PRE_LINE_ITEMS + "; multi-file GLIM asset",
        "1. Open GLIM for asset with multiple playable files\n"
        "2. Locate Play All control\n"
        "3. Click Play All\n"
        "4. Observe playback sequence across files",
        "Play All starts sequential playback of all associated files without manual re-selection",
        "ValidateGlimPlayAllFiles", "validateGlimPlayAllFiles", "enable-play-all-files-in-glim",
    ),
    row(
        "030", "Test endpoint connection feature (enable-test-endpoint-connection-feature)", "Admin",
        "3 - Medium", PRE_BASE + "; admin or settings access",
        "1. Navigate to Admin / Settings area with endpoint configuration\n"
        "2. Locate Test Connection or Test Endpoint action\n"
        "3. Run test against configured endpoint\n"
        "4. Review success/failure message",
        "Test endpoint connection runs and returns clear pass/fail result without JS error",
        "ValidateTestEndpointConnection", "validateTestEndpointConnection",
        "enable-test-endpoint-connection-feature",
    ),
    row(
        "031", "End-to-end smoke — login through Orders, LI, filters, logout", "Regression Smoke",
        "1 - Critical", PRE_BASE,
        "1. Log in via Okta\n"
        "2. Orders tab → apply one filter → open one order Details\n"
        "3. Switch to Line Items tab → apply one filter\n"
        "4. Open Table View on each tab and close\n"
        "5. Log out or close session",
        "Full smoke path completes without blocking errors; both tabs functional after flag cleanup",
        "ValidateEndToEndSmoke", "validateEndToEndSmoke", "multiple",
    ),
    row(
        "032", "No new console errors on primary views", "Regression Smoke",
        "2 - High", PRE_BASE,
        "1. Log in with DevTools Console open\n"
        "2. Visit Orders tab — note errors\n"
        "3. Visit Line Items tab — note errors\n"
        "4. Expand 3 left filters on each tab\n"
        "5. Open and close order Details once",
        "No new uncaught JS exceptions or flag-related errors (e.g. undefined feature flag keys) on primary views",
        "ValidateNoConsoleErrorsOnPrimaryViews", "validateNoConsoleErrorsOnPrimaryViews", "multiple",
    ),
]


def write_csv(path: Path, rows: list) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=HEADER, quoting=csv.QUOTE_MINIMAL)
        writer.writeheader()
        writer.writerows(rows)


def main():
    write_csv(OUT / "FF_LD_BSD28459_FeatureFlagCleanup_ManualCases_Import.csv", CASES)
    print(f"Generated {len(CASES)} cases -> {OUT / 'FF_LD_BSD28459_FeatureFlagCleanup_ManualCases_Import.csv'}")


if __name__ == "__main__":
    main()
