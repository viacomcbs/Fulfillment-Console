#!/usr/bin/env python3
"""Generate TestRail CSV import files for Left Filter manual test cases."""
import csv
import io
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "testrail"

FILTERS = [
    ("LINE_ITEM_STATUS", "Line Item Status", "LineItemStatus", "CHECKBOX"),
    ("ORDER_STATUS", "Order Status", "OrderStatus", "CHECKBOX"),
    ("ENVIRONMENT", "Environment", "Environment", "CHECKBOX"),
    ("JOB_TYPE", "Job type", "JobType", "CHECKBOX"),
    ("SUBMITTED_BY", "Submitted by", "SubmittedBy", "CHECKBOX"),
    ("PARTNER", "Partner", "Partner", "LARGE_LIST"),
    ("SERIES_TITLE", "Series title", "SeriesTitle", "CHECKBOX"),
    ("FLAG", "Flag", "Flag", "CHECKBOX"),
    ("ASSIGNED_TO", "Assigned To", "AssignedTo", "CHECKBOX"),
    ("SEASON_NUMBER", "Season number", "SeasonNumber", "RANGE"),
    ("REGION", "Region", "Region", "CHECKBOX"),
    ("EPISODE_NUMBER", "Episode number", "EpisodeNumber", "RANGE"),
    ("BRAND", "Brand", "Brand", "CHECKBOX"),
    ("SYSTEM_NAME", "System Name", "SystemName", "CHECKBOX"),
    ("LANGUAGE", "Language", "Language", "CHECKBOX"),
    ("DEMAND_SYSTEM", "Demand system", "DemandSystem", "CHECKBOX"),
    ("ERROR_MESSAGE", "Error message", "ErrorMessage", "CHECKBOX"),
    ("FRANCHISE", "Franchise", "Franchise", "CHECKBOX"),
    ("DELIVERY_PROTOCOL", "Delivery Protocol", "DeliveryProtocol", "CHECKBOX"),
    ("ACTIVITY_TYPE", "Activity Type", "ActivityType", "CHECKBOX"),
    ("CONTENT_TYPE", "Content type", "ContentType", "CHECKBOX"),
]

SKIP = {
    "RANGE": {"OPTION_ORDER", "SELECT_ALL", "SCROLL", "ACTIVE_FILTERS", "CLEAR_FILTERS"},
    "LARGE_LIST": {"SELECT_ALL", "TABLE_SYNC"},
}

CATEGORIES = [
    ("OPTION_ORDER", 101, "OptionOrder", "optionOrder", "Option list order", "2 - High"),
    ("BASIC", 100, "Basic", "basic", "Basic", "1 - Critical"),
    ("SEARCH", 200, "Search", "search", "Search", "2 - High"),
    ("SELECT_ALL", 400, "SelectAll", "selectAll", "Select all", "2 - High"),
    ("TABLE_SYNC", 600, "TableSync", "tableSync", "Table sync", "1 - Critical"),
    ("SCROLL", 800, "Scroll", "scroll", "Scroll", "3 - Medium"),
    ("ACTIVE_FILTERS", 1000, "ActiveFilters", "activeFilters", "Active filters", "2 - High"),
    ("CLEAR_FILTERS", 1100, "ClearFilters", "clearFilters", "Clear filters", "2 - High"),
]

TAB_LEVEL = [
    ("001", "All left filters available", "Validate all 21 left filters are listed in the panel",
     "1. Log in to Fulfillment Console\n2. Set calendar to Yesterday\n3. Navigate to {tab} tab\n4. Review left filter panel accordion list",
     "All 21 filters are visible: Line Item Status, Order Status, Environment, Job type, Submitted by, Partner, Series title, Flag, Assigned To, Season number, Region, Episode number, Brand, System Name, Language, Demand system, Error message, Franchise, Delivery Protocol, Activity Type, Content type"),
    ("002", "Expand and collapse filter", "Validate expand/collapse accordion on a filter",
     "1. On {tab} tab, locate Order Status filter\n2. Click accordion to expand\n3. Verify options load\n4. Click accordion to collapse",
     "Filter expands to show options; collapses and hides options"),
    ("003", "Options available on expand", "Validate filter options load when expanded",
     "1. On {tab} tab, expand Order Status filter\n2. Wait for options to load",
     "Filter options are displayed (checkbox list or range inputs as applicable)"),
    ("004", "Options alphabetical order", "Validate non-zero count options sort A-Z",
     "1. On {tab} tab, expand Order Status filter\n2. Review visible option labels with counts > 0",
     "Non-zero count options appear in alphabetical order within the visible viewport"),
    ("005", "Non-zero first, divider, zero count", "Validate option grouping order",
     "1. On {tab} tab, expand Order Status filter\n2. Review option list top to bottom",
     "Select All at top; non-zero count options first; divider line; zero-count options last (A-Z within each group)"),
    ("006", "Select All at top", "Validate Select All is first option",
     "1. On {tab} tab, expand Order Status filter\n2. Check first row below search box",
     "Select All checkbox/label is at the top of the option list"),
    ("007", "Select All selects all options", "Validate Select All selects every option",
     "1. On {tab} tab, expand Order Status filter\n2. Click Select All\n3. Review checked options",
     "All visible options are selected; selection count updates"),
    ("008", "Select All indeterminate state", "Validate partial selection and deselect",
     "1. On {tab} tab, expand Order Status\n2. Select one option manually\n3. Observe Select All state\n4. Deselect all",
     "Select All shows indeterminate when partially selected; deselect clears all selections"),
    ("009", "Search inside filter", "Validate in-filter search on Order Status",
     "1. On {tab} tab, expand Order Status\n2. Type partial text (e.g. deliver) in filter search\n3. Review results\n4. Clear search",
     "Options filter to matching labels; clear restores full list"),
    ("010", "Select All disabled (>1000 options)", "Validate Select All disabled on Partner filter",
     "1. On {tab} tab, expand Partner filter\n2. Locate Select All control",
     "Select All is disabled or not offered when option count exceeds 1000"),
    ("011", "Scroll filter options", "Validate virtual scroll on Partner filter",
     "1. On {tab} tab, expand Partner filter\n2. Scroll through option list using mouse wheel or scrollbar",
     "Options scroll smoothly without UI error; additional options load in viewport"),
    ("012", "Pagination inside filter", "Validate pagination on Partner filter",
     "1. On {tab} tab, expand Partner filter\n2. Use pagination controls if present\n3. Navigate next/previous pages",
     "Pagination loads next set of partner options without error"),
    ("013", "Table record count reflects filter", "Validate grid count matches filter option count",
     "1. On {tab} tab, expand Order Status\n2. Note count on an option (e.g. Done: Delivered)\n3. Select that option\n4. Compare to table total count label",
     "Table total record count matches the count shown on the selected filter option (or both zero)"),
    ("014", "Table records match filter", "Validate grid rows match selected filter value",
     "1. On {tab} tab, select one Order Status option\n2. Review visible grid rows and Status column",
     "Displayed rows match the selected filter value in the corresponding table column"),
    ("015", "Filter names match table columns", "Validate filter labels align with grid columns",
     "1. On {tab} tab, review left filter names\n2. Open Table View / compare to grid column headers",
     "Left filter names correspond to available table column names where applicable"),
    ("016", "Selected options in Active Filters", "Validate active filter chips",
     "1. On {tab} tab, select one or more options in Order Status\n2. Review Active Filters area above grid",
     "Selected options appear as chips/tags in Active Filters section"),
    ("017", "Filter count inside and outside", "Validate selection count inside panel vs outside badge",
     "1. On {tab} tab, expand Order Status and select options\n2. Compare count inside filter vs outside accordion badge",
     "Inside and outside selection counts are consistent"),
]

HEADER = [
    "Title", "Section", "Template", "Type", "Priority", "References",
    "Automation Type", "Automation ID", "Preconditions", "Steps", "Expected Result",
]


def folder(enum_name: str) -> str:
    return enum_name.lower().replace("_", "")


def tc_id(cat_key: str, offset: int, index: int) -> int:
    if cat_key == "OPTION_ORDER":
        return 101 + index
    return offset + index


def skip_cat(kind: str, cat_key: str, tab: str, enum_name: str) -> bool:
    if cat_key in SKIP.get(kind, set()):
        return True
    if cat_key == "OPTION_ORDER" and kind != "CHECKBOX":
        return True
    if tab == "orders" and enum_name == "PARTNER" and cat_key == "SEARCH":
        return True  # custom LF_O_TC201_Partner_SearchTest
    return False


def automation_id(tab: str, enum_name: str, pascal: str, cat_suffix: str, tc: int, method: str) -> str:
    pkg_tab = "orders" if tab == "orders" else "lineitems"
    prefix = "O" if tab == "orders" else "LI"
    if tab == "orders" and enum_name == "PARTNER" and cat_suffix == "Search":
        return "com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner.LF_O_TC201_Partner_SearchTest#tc201_partner_searchAllFromJson"
    cls = f"LF_{prefix}_TC{tc}_{pascal}_{cat_suffix}Test"
    return (
        f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter."
        f"{pkg_tab}.{folder(enum_name)}.{cls}#{method}"
    )


def steps_basic(tab_label: str, filter_name: str, kind: str) -> tuple[str, str]:
    if kind == "RANGE":
        return (
            f"1. Log in; set calendar to Yesterday\n2. Navigate to {tab_label} tab\n3. Expand {filter_name} filter\n4. Verify From/To range inputs present",
            f"{filter_name} filter expands; range input fields are visible and usable",
        )
    return (
        f"1. Log in; set calendar to Yesterday\n2. Navigate to {tab_label} tab\n3. Verify {filter_name} is visible in left panel\n4. Expand {filter_name}\n5. Confirm options load and Select All at top\n6. Collapse filter",
        f"{filter_name} is visible; expands/collapses; options and Select All at top display correctly",
    )


def steps_for(cat_key: str, tab_label: str, filter_name: str, kind: str) -> tuple[str, str]:
    if cat_key == "BASIC":
        return steps_basic(tab_label, filter_name, kind)
    templates = {
        "OPTION_ORDER": (
            f"1. On {tab_label} tab, expand {filter_name}\n2. Verify Select All is first\n3. Review non-zero options order\n4. Check divider before zero-count options if present\n5. Verify zero-count options A-Z",
            "Select All first; non-zero A-Z; divider; zero-count A-Z in visible viewport",
        ),
        "SEARCH": (
            f"1. On {tab_label} tab, expand {filter_name}\n2. Note first visible option label\n3. Type into filter search box\n4. Verify filtered results\n5. Clear search",
            "Search returns matching options; clear restores list",
        ),
        "SELECT_ALL": (
            f"1. On {tab_label} tab, expand {filter_name}\n2. Click Select All\n3. Verify all options selected and counts\n4. Deselect one option — check indeterminate\n5. Deselect all",
            "Select All selects all; indeterminate on partial; counts inside/outside match",
        ),
        "TABLE_SYNC": (
            f"1. On {tab_label} tab, expand {filter_name}\n2. Select one option with non-zero count\n3. Compare filter option count to table total count\n4. Verify first visible grid rows match filter column",
            "Table record count matches filter count; grid rows reflect selected filter value",
        ),
        "SCROLL": (
            f"1. On {tab_label} tab, expand {filter_name}\n2. Scroll option list vertically\n3. Confirm no UI errors and options continue loading",
            "Virtual scroll works without error; additional options appear in viewport",
        ),
        "ACTIVE_FILTERS": (
            f"1. On {tab_label} tab, select one option in {filter_name}\n2. Select a second option (multi-select)\n3. Review Active Filters chips above grid",
            "Active Filters chips show single and multiple selections from {filter_name}",
        ),
        "CLEAR_FILTERS": (
            f"1. On {tab_label} tab, select options in {filter_name}\n2. Remove chip from Active Filters OR click Clear\n3. Verify selections cleared and grid resets",
            "Filter selections cleared; Active Filters empty; grid shows unfiltered data",
        ),
    }
    steps, expected = templates[cat_key]
    return steps, expected.replace("{filter_name}", filter_name)


def add_row(rows, title, section, priority, ref, auto_id, pre, steps, expected):
    rows.append({
        "Title": title,
        "Section": section,
        "Template": "Test Case (Text)",
        "Type": "Functional",
        "Priority": priority,
        "References": ref,
        "Automation Type": "Java TestNG",
        "Automation ID": auto_id,
        "Preconditions": pre,
        "Steps": steps,
        "Expected Result": expected,
    })


def tab_level_rows(tab: str) -> list:
    tab_label = "Orders" if tab == "orders" else "Line Items"
    prefix = "O" if tab == "orders" else "LI"
    rows = []
    for num, short, summary, steps_tpl, expected in TAB_LEVEL:
        manual_id = f"LF_{prefix}_{num}"
        auto_cls = f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.{tab}.LF_{prefix}_{num}_Validate{short.replace(' ', '')}"
        # fix class names - use known pattern from files
        rows.append(None)  # placeholder
    return rows


def build_tab_level(tab: str) -> list:
    tab_label = "Orders" if tab == "orders" else "Line Items"
    prefix = "O" if tab == "orders" else "LI"
    section = f"Fulfillment Console > Left Filters > {tab_label} > Tab Level Regression"
    pre = f"DEV/PROD Fulfillment Console; calendar = Yesterday; {tab_label} tab"
    rows = []
    class_names = {
        "001": "ValidateAllLeftFiltersAvailable",
        "002": "ValidateExpandCollapseIcon",
        "003": "ValidateOptionsAvailableOnExpand",
        "004": "ValidateOptionsAlphabeticalOrder",
        "005": "ValidateNonZeroCountFirstThenDividerThenZeroCount",
        "006": "ValidateSelectAllOptionAtTop",
        "007": "ValidateSelectAllSelectsAll",
        "008": "ValidateSelectAllIndeterminateState",
        "009": "ValidateSearchInsideFilter",
        "010": "ValidateSelectAllDisabledWhenMoreThan1000Options",
        "011": "ValidateScrollFilterOptions",
        "012": "ValidatePaginationWorks",
        "013": "ValidateTableRecordCountReflectsFilter",
        "014": "ValidateTableRecordsMatchFilter",
        "015": "ValidateFilterNamesMatchTableColumns",
        "016": "ValidateSelectedOptionsInActiveFilters",
        "017": "ValidateFilterCountInsideAndOutside",
    }
    method_names = {
        "001": "validateAllLeftFiltersAvailable",
        "002": "validateExpandCollapseWorks",
        "003": "validateOptionsAvailableOnExpand",
        "004": "validateOptionsAlphabeticalOrder",
        "005": "validateNonZeroCountFirstThenDividerThenZeroCount",
        "006": "validateSelectAllAtTop",
        "007": "validateSelectAllSelectsAll",
        "008": "validateSelectAllIndeterminateState",
        "009": "validateSearchInsideFilter",
        "010": "validateSelectAllDisabledWhenMoreThan1000Options",
        "011": "validateScrollFilterOptions",
        "012": "validatePaginationWorks",
        "013": "validateTableRecordCountReflectsFilter",
        "014": "validateTableRecordsMatchFilter",
        "015": "validateFilterNamesMatchTableColumns",
        "016": "validateSelectedOptionsInActiveFilters",
        "017": "validateFilterCountInsideAndOutside",
    }
    for num, short, summary, steps_tpl, expected in TAB_LEVEL:
        cls = class_names[num]
        method = method_names[num]
        auto = f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.{tab}.LF_{prefix}_{num}_{cls}#{method}"
        add_row(
            rows,
            f"LF_{prefix}_{num} — {tab_label}: {short}",
            section,
            "2 - High" if num not in ("001", "013", "014") else "1 - Critical",
            "Left Filters",
            auto,
            pre,
            steps_tpl.format(tab=tab_label),
            expected,
        )
    return rows


def build_per_filter(tab: str) -> list:
    tab_label = "Orders" if tab == "orders" else "Line Items"
    prefix = "O" if tab == "orders" else "LI"
    rows = []
    pre = f"DEV/PROD Fulfillment Console; calendar = Yesterday; {tab_label} tab"
    for index, (enum_name, display, pascal, kind) in enumerate(FILTERS, start=1):
        section = f"Fulfillment Console > Left Filters > {tab_label} > Per Filter > {display}"
        for cat_key, offset, cat_suffix, method_suffix, label, priority in CATEGORIES:
            if skip_cat(kind, cat_key, tab, enum_name):
                continue
            tc = tc_id(cat_key, offset, index)
            manual_id = f"LF_{prefix}_TC{tc}"
            method = f"tc{tc}_{pascal[0].lower()}{pascal[1:]}_{method_suffix}"
            if tab == "orders" and enum_name == "ORDER_STATUS" and cat_key == "BASIC":
                method = "tc101_orderStatus_basic"
            elif tab == "orders" and enum_name == "ORDER_STATUS" and cat_key == "OPTION_ORDER":
                method = "tc102_orderStatus_optionOrder"
            elif tab == "orders" and enum_name == "ORDER_STATUS" and cat_key == "SEARCH":
                method = "tc201_orderStatus_search"
            steps, expected = steps_for(cat_key, tab_label, display, kind)
            if cat_key == "TABLE_SYNC" and tab == "orders" and enum_name == "ORDER_STATUS":
                steps = (
                    f"1. On Orders tab, expand Order Status\n2. Select Done: Delivered\n3. Compare filter count to table total\n4. Verify Status column on first rows shows Delivered"
                )
                expected = "Table count matches filter count; Status cells show Delivered when count > 0"
            auto = automation_id(tab, enum_name, pascal, cat_suffix, tc, method)
            add_row(
                rows,
                f"{manual_id} — {tab_label} {display}: {label}",
                section,
                priority,
                "Left Filters",
                auto,
                pre,
                steps,
                expected,
            )
        if tab == "orders" and enum_name == "PARTNER":
            add_row(
                rows,
                "LF_O_TC201 — Orders Partner: Search (duplicate check)",
                section,
                "1 - Critical",
                "Left Filters",
                automation_id(tab, enum_name, pascal, "Search", 201, "tc201_partner_searchAllFromJson"),
                pre + "; Partner options JSON collected",
                "1. On Orders tab, expand Partner\n2. For each partner name from collection JSON, type exact name in search\n3. Verify exactly one matching option appears",
                "Each partner name returns exactly one exact-match option (no duplicates in search results)",
            )
    return rows


def write_csv(path: Path, rows: list) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=HEADER, quoting=csv.QUOTE_MINIMAL)
        writer.writeheader()
        writer.writerows(rows)


def main():
    orders_tab = build_tab_level("orders")
    li_tab = build_tab_level("lineitems")
    orders_pf = build_per_filter("orders")
    li_pf = build_per_filter("lineitems")

    write_csv(OUT / "LF_Orders_TabLevel_ManualCases_Import.csv", orders_tab)
    write_csv(OUT / "LF_LineItems_TabLevel_ManualCases_Import.csv", li_tab)
    write_csv(OUT / "LF_Orders_PerFilter_ManualCases_Import.csv", orders_pf)
    write_csv(OUT / "LF_LineItems_PerFilter_ManualCases_Import.csv", li_pf)

    all_rows = orders_tab + li_tab + orders_pf + li_pf
    write_csv(OUT / "LF_LeftFilter_All_ManualCases_Import.csv", all_rows)

    print(f"Tab level Orders: {len(orders_tab)}")
    print(f"Tab level Line Items: {len(li_tab)}")
    print(f"Per-filter Orders: {len(orders_pf)}")
    print(f"Per-filter Line Items: {len(li_pf)}")
    print(f"Combined Left Filter: {len(all_rows)}")
    print(f"Output: {OUT}")


if __name__ == "__main__":
    main()
