#!/usr/bin/env python3
"""Generate per-filter left panel test classes (Orders + Line Items)."""
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter"

FILTERS = [
    ("LINE_ITEM_STATUS", "LineItemStatus", "CHECKBOX"),
    ("ORDER_STATUS", "OrderStatus", "CHECKBOX"),
    ("ENVIRONMENT", "Environment", "CHECKBOX"),
    ("JOB_TYPE", "JobType", "CHECKBOX"),
    ("SUBMITTED_BY", "SubmittedBy", "CHECKBOX"),
    ("PARTNER", "Partner", "LARGE_LIST"),
    ("SERIES_TITLE", "SeriesTitle", "CHECKBOX"),
    ("FLAG", "Flag", "CHECKBOX"),
    ("ASSIGNED_TO", "AssignedTo", "CHECKBOX"),
    ("SEASON_NUMBER", "SeasonNumber", "RANGE"),
    ("REGION", "Region", "CHECKBOX"),
    ("EPISODE_NUMBER", "EpisodeNumber", "RANGE"),
    ("BRAND", "Brand", "CHECKBOX"),
    ("SYSTEM_NAME", "SystemName", "CHECKBOX"),
    ("LANGUAGE", "Language", "CHECKBOX"),
    ("DEMAND_SYSTEM", "DemandSystem", "CHECKBOX"),
    ("ERROR_MESSAGE", "ErrorMessage", "CHECKBOX"),
    ("FRANCHISE", "Franchise", "CHECKBOX"),
    ("DELIVERY_PROTOCOL", "DeliveryProtocol", "CHECKBOX"),
    ("ACTIVITY_TYPE", "ActivityType", "CHECKBOX"),
    ("CONTENT_TYPE", "ContentType", "CHECKBOX"),
]

SKIP_CATEGORIES = {
    "RANGE": {"OPTION_ORDER", "SELECT_ALL", "SCROLL", "ACTIVE_FILTERS", "CLEAR_FILTERS"},
    "LARGE_LIST": {"SELECT_ALL", "TABLE_SYNC"},
    "CHECKBOX": set(),
}

EXISTING = {
    ("orders", "orderstatus"),
    ("orders", "partner"),  # partner has custom search only; other categories still generated
}

SKIP_FILES = {
    ("orders", "partner", "SEARCH"),  # LF_O_TC201_Partner_SearchTest exists
    ("orders", "orderstatus", "*"),
}

CATEGORIES = [
    ("BASIC", 100, "basic", "Basic", "Basic"),
    ("OPTION_ORDER", 101, "optionOrder", "Option list order", "OptionOrder"),
    ("SEARCH", 200, "search", "Search", "Search"),
    ("SELECT_ALL", 400, "selectAll", "Select all", "SelectAll"),
    ("TABLE_SYNC", 600, "tableSync", "Table sync", "TableSync"),
    ("SCROLL", 800, "scroll", "Scroll", "Scroll"),
    ("ACTIVE_FILTERS", 1000, "activeFilters", "Active filters", "ActiveFilters"),
    ("CLEAR_FILTERS", 1100, "clearFilters", "Clear filters", "ClearFilters"),
]


def long_path(path: Path) -> str:
    resolved = str(path.resolve())
    if os.name == "nt" and not resolved.startswith("\\\\?\\"):
        return "\\\\?\\" + resolved
    return resolved


def write_file(path: Path, content: str) -> None:
    os.makedirs(long_path(path.parent), exist_ok=True)
    with open(long_path(path), "w", encoding="utf-8") as handle:
        handle.write(content)


def folder_name(enum_name: str) -> str:
    return enum_name.lower().replace("_", "")


def camel_method(prefix: str, pascal: str, suffix: str) -> str:
    return f"{prefix}_{pascal[0].lower()}{pascal[1:]}_{suffix}"


def should_skip(tab: str, folder: str, cat_key: str, kind: str) -> bool:
    if (tab, folder, "*") in SKIP_FILES:
        return True
    if (tab, folder, cat_key) in SKIP_FILES:
        return True
    if cat_key == "OPTION_ORDER" and kind != "CHECKBOX":
        return True
    if cat_key in SKIP_CATEGORIES.get(kind, set()):
        return True
    return False


def body_basic(tab_cfg, kind):
    tab = tab_cfg["console"]
    if kind == "CHECKBOX":
        return f"""        leftFilterPanelUtil.navigateToTab(ConsoleTab.{tab});
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);"""
    return f"""        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.{tab},
                FILTER, LeftFilterTestCategory.BASIC, FILTER_INDEX);"""


def body_option_order(tab_cfg):
    tab = tab_cfg["console"]
    return f"""        leftFilterPanelUtil.navigateToTab(ConsoleTab.{tab});
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);"""


def body_search(tab_cfg):
    tab = tab_cfg["console"]
    return f"""        leftFilterPanelUtil.navigateToTab(ConsoleTab.{tab});
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);"""


def body_runner(tab_cfg, category_enum):
    tab = tab_cfg["console"]
    return f"""        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.{tab},
                FILTER, LeftFilterTestCategory.{category_enum}, FILTER_INDEX);"""


def generate_class(tab_cfg, enum_name, pascal, kind, index, cat_key, offset, method_suffix, desc_suffix, class_suffix):
    tab = tab_cfg["tab"]
    folder = folder_name(enum_name)
    if should_skip(tab, folder, cat_key, kind):
        return None

    tc_id = offset + index if cat_key != "OPTION_ORDER" else 101 + index
    prefix = tab_cfg["prefix"]
    filter_enum = tab_cfg["filter_enum"]
    base_test = tab_cfg["base_test"]
    package = f"com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.{tab}.{folder}"

    if cat_key == "BASIC":
        body = body_basic(tab_cfg, kind)
        imports = ""
        if kind != "CHECKBOX":
            imports = """
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;"""
    elif cat_key == "OPTION_ORDER":
        body = body_option_order(tab_cfg)
        imports = ""
    elif cat_key == "SEARCH":
        body = body_search(tab_cfg)
        imports = ""
    else:
        body = body_runner(tab_cfg, cat_key)
        imports = """
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;"""

    method = camel_method(f"tc{tc_id}", pascal, method_suffix)
    class_name = f"LF_{prefix}_TC{tc_id}_{pascal}_{class_suffix}Test"

    content = f"""package {package};

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.{filter_enum};{imports}
import {base_test};
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC{tc_id} — {pascal} {desc_suffix}. */
public class {class_name} extends {base_test.split(".")[-1]} {{

    private static final String FILTER = {filter_enum}.{enum_name}.getDisplayName();
    private static final int FILTER_INDEX = {index};

    @Test(priority = 1)
    @Description("TC{tc_id}: {pascal} — {desc_suffix}")
    public void {method}() throws InterruptedException {{
        softAssert = new SoftAssert("{method}", getClass().getSimpleName());
{body}
        softAssert.assertAll();
    }}
}}
"""
    out_dir = BASE / tab / folder
    out_file = out_dir / f"{class_name}.java"
    if out_file.exists():
        return None
    write_file(out_file, content)
    return str(out_file.relative_to(ROOT)).replace("\\", "/")


def main():
    tabs = [
        {
            "tab": "orders",
            "prefix": "O",
            "console": "ORDERS",
            "filter_enum": "OrdersLeftFilter",
            "base_test": "com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest",
            "kind_param": "CHECKBOX",
        },
        {
            "tab": "lineitems",
            "prefix": "LI",
            "console": "LINE_ITEMS",
            "filter_enum": "LineItemsLeftFilter",
            "base_test": "com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest",
            "kind_param": "CHECKBOX",
        },
    ]

    created = []
    errors = []
    for tab_cfg in tabs:
        for i, (enum_name, pascal, kind) in enumerate(FILTERS, start=1):
            tab_cfg["kind_param"] = kind
            for cat_key, offset, method_suffix, desc_suffix, class_suffix in CATEGORIES:
                try:
                    path = generate_class(tab_cfg, enum_name, pascal, kind, i, cat_key, offset, method_suffix, desc_suffix, class_suffix)
                    if path:
                        created.append(path)
                except OSError as exc:
                    errors.append(f"{tab_cfg['tab']}/{folder_name(enum_name)}/{cat_key}: {exc}")

    write_suite_xml()
    print(f"Created {len(created)} new test classes")
    if errors:
        print(f"Errors ({len(errors)}):")
        for err in errors[:10]:
            print(f"  {err}")


def write_suite_xml():
    suite_path = ROOT / "src/test/resources/LeftFilterPerFilter_Dedicated_DevServerSuite.xml"
    lines = [
        '<!-- Dedicated per-filter left panel tests (generated). Run subset or full suite on DEV. -->',
        '<suite name="Left Filter Per-Filter Dedicated DEV" parallel="false" preserve-order="true">',
        "",
        '    <parameter name="RunAsFactory" value="false"/>',
        '    <parameter name="Browser" value="Chrome"/>',
        '    <parameter name="TestEnvironment" value="DEV"/>',
        '    <parameter name="SendReportAutoEmails" value="false"/>',
        "",
        '    <listeners>',
        '        <listener class-name="com.paramount.test.ff.common.listeners.AllureListeners"/>',
        '        <listener class-name="com.paramount.test.ff.common.listeners.SuiteListeners"/>',
        '        <listener class-name="com.paramount.test.ff.common.listeners.LeftFilterSuiteListener"/>',
        '    </listeners>',
        "",
    ]
    for tab in ("orders", "lineitems"):
        tab_label = "Orders" if tab == "orders" else "Line Items"
        lines.append(f'    <test verbose="1" name="{tab_label} - Per-filter dedicated" preserve-order="true">')
        lines.append("        <classes>")
        for f in sorted((BASE / tab).rglob("LF_*.java")):
            rel = f.relative_to(ROOT / "src/test/java").with_suffix("").as_posix().replace("/", ".")
            lines.append(f'            <class name="{rel}"/>')
        lines.append("        </classes>")
        lines.append("    </test>")
        lines.append("")
    lines.append("</suite>")
    suite_path.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote suite: {suite_path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
