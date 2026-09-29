#!/usr/bin/env python3
"""Generate LF_O_Flow_*Test.java — one continuous-flow test class per Orders left-filter folder."""

from __future__ import annotations

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
JAVA_ORDERS = (
    PROJECT_ROOT
    / "src/test/java/com/paramount/test/ff/uitests/tests/leftfiltersvalidation/perfilter/orders"
)
SKIP_DIRS = {"aggregated", "jobtype"}

FILTER_ENUM = {
    "activitytype": ("ACTIVITY_TYPE", "LeftFilterOrdersTabBaseTest"),
    "assignedto": ("ASSIGNED_TO", "LeftFilterAssignedToOrdersTabBaseTest"),
    "brand": ("BRAND", "LeftFilterBrandOrdersTabBaseTest"),
    "contenttype": ("CONTENT_TYPE", "LeftFilterContentTypeOrdersTabBaseTest"),
    "deliveryprotocol": ("DELIVERY_PROTOCOL", "LeftFilterDeliveryProtocolOrdersTabBaseTest"),
    "demandsystem": ("DEMAND_SYSTEM", "LeftFilterDemandSystemOrdersTabBaseTest"),
    "environment": ("ENVIRONMENT", "LeftFilterEnvironmentOrdersTabBaseTest"),
    "episodenumber": ("EPISODE_NUMBER", "LeftFilterEpisodeNumberOrdersTabBaseTest"),
    "errormessage": ("ERROR_MESSAGE", "LeftFilterErrorMessageOrdersTabBaseTest"),
    "flag": ("FLAG", "LeftFilterFlagOrdersTabBaseTest"),
    "franchise": ("FRANCHISE", "LeftFilterFranchiseOrdersTabBaseTest"),
    "job": ("JOB", "LeftFilterJobOrdersTabBaseTest"),
    "language": ("LANGUAGE", "LeftFilterLanguageOrdersTabBaseTest"),
    "lineitemstatus": ("LINE_ITEM_STATUS", "LeftFilterLineItemStatusOrdersTabBaseTest"),
    "orderstatus": ("ORDER_STATUS", "LeftFilterOrderStatusOrdersTabBaseTest"),
    "partner": ("PARTNER", "LeftFilterPartnerOrdersTabBaseTest"),
    "region": ("REGION", "LeftFilterRegionOrdersTabBaseTest"),
    "seasonnumber": ("SEASON_NUMBER", "LeftFilterSeasonNumberOrdersTabBaseTest"),
    "seriestitle": ("SERIES_TITLE", "LeftFilterSeriesTitleOrdersTabBaseTest"),
    "submittedby": ("SUBMITTED_BY", "LeftFilterSubmittedByOrdersTabBaseTest"),
    "systemname": ("SYSTEM_NAME", "LeftFilterSystemNameOrdersTabBaseTest"),
}

INDEX_RE = re.compile(r"FILTER_INDEX\s*=\s*(\d+)")


def read_filter_index(folder: Path) -> int:
    for java_file in sorted(folder.glob("LF_O_TC*.java")):
        text = java_file.read_text(encoding="utf-8")
        match = INDEX_RE.search(text)
        if match:
            return int(match.group(1))
    raise RuntimeError(f"No FILTER_INDEX found in {folder}")


def pascal_filter_dir(filter_dir: str) -> str:
    return "".join(part.capitalize() for part in filter_dir.split("_"))


def build_flow_test(filter_dir: str) -> None:
    enum_name, base_class = FILTER_ENUM[filter_dir]
    folder = JAVA_ORDERS / filter_dir
    filter_index = read_filter_index(folder)
    class_name = f"LF_O_Flow_{pascal_filter_dir(filter_dir)}Test"
    package_suffix = filter_dir
    label = pascal_filter_dir(filter_dir)

    base_import = ""
    if base_class == "LeftFilterOrdersTabBaseTest":
        base_import = (
            "import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders."
            "LeftFilterOrdersTabBaseTest;\n"
        )

    content = f"""package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.{package_suffix};

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterContinuousFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
{base_import}import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Continuous flow — all scenarios in one browser session (no refresh between steps). */
public class {class_name} extends {base_class} {{

    private static final String FILTER = OrdersLeftFilter.{enum_name}.getDisplayName();
    private static final int FILTER_INDEX = {filter_index};

    @Test(priority = 1)
    @Description("{label} — continuous flow (Basic → Option order → Scroll → Select all → Search → Table sync → Active filters → Clear filters)")
    public void flow_{filter_dir}AllScenariosContinuous() throws InterruptedException {{
        softAssert = new SoftAssert("flow_{filter_dir}AllScenariosContinuous", getClass().getSimpleName());
        LeftFilterContinuousFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }}
}}
"""
    out = folder / f"{class_name}.java"
    out.write_text(content, encoding="utf-8")
    print(f"Wrote {out.relative_to(PROJECT_ROOT)}")


def main() -> None:
    for filter_dir in sorted(d.name for d in JAVA_ORDERS.iterdir() if d.is_dir() and d.name not in SKIP_DIRS):
        if filter_dir not in FILTER_ENUM:
            print(f"Skip unknown filter folder: {filter_dir}")
            continue
        build_flow_test(filter_dir)


if __name__ == "__main__":
    main()
