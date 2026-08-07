#!/usr/bin/env python3
"""Generate BSD-28459 FF_LD_* TestNG test classes from TestRail CSV automation IDs."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src" / "test" / "java" / "com" / "paramount" / "test" / "ff" / "uitests" / "tests" / "featureflag" / "bsd28459"
PKG = "com.paramount.test.ff.uitests.tests.featureflag.bsd28459"

CASES = [
    ("001", "ValidateAppBootstrapAfterOktaLogin", "validateAppBootstrapAfterOktaLogin",
     "FF_LD_001 — App bootstrap after Okta login", "validationUtil.validateAppBootstrapAfterOktaLogin(softAssert);", False),
    ("002", "ValidateOktaTokenInHttpHeaders", "validateOktaTokenInHttpHeaders",
     "FF_LD_002 — Okta token sent on API requests", "validationUtil.validateOktaTokenInHttpHeaders(leftFilterPanelUtil, softAssert);", False),
    ("003", "ValidateOrdersViewDynamicGraphql", "validateOrdersViewDynamicGraphql",
     "FF_LD_003 — Orders view dynamic GraphQL", "validationUtil.validateOrdersViewDynamicGraphql(leftFilterPanelUtil, softAssert);", False),
    ("004", "ValidateMaterialIdAvailableInOrdersTableView", "validateMaterialIdAvailableInOrdersTableView",
     "FF_LD_004 — Material ID in Orders Table View", "validationUtil.validateMaterialIdAvailableInOrdersTableView(softAssert);", False),
    ("005", "ValidateMaterialIdVisibleInOrdersGrid", "validateMaterialIdVisibleInOrdersGrid",
     "FF_LD_005 — Material ID visible in Orders grid", "validationUtil.validateMaterialIdVisibleInOrdersGrid(softAssert);", False),
    ("006", "ValidateEditCridOnOrdersRow", "validateEditCridOnOrdersRow",
     "FF_LD_006 — Edit CRID on Orders row", "validationUtil.validateEditCridOnOrdersRow(softAssert);", False),
    ("007", "ValidatePartnerEndDateColumnInOrders", "validatePartnerEndDateColumnInOrders",
     "FF_LD_007 — Partner End Date column in Orders", "validationUtil.validatePartnerEndDateColumnInOrders(softAssert);", False),
    ("008", "ValidateUuidDisplayedInOrdersView", "validateUuidDisplayedInOrdersView",
     "FF_LD_008 — UUID displayed in Orders view", "validationUtil.validateUuidDisplayedInOrdersView(softAssert);", False),
    ("009", "ValidateOrderHistoryV2InOrderDetails", "validateOrderHistoryV2InOrderDetails",
     "FF_LD_009 — Order History v2 in Details", "validationUtil.validateOrderHistoryV2InOrderDetails(leftFilterPanelUtil, softAssert);", False),
    ("010", "ValidateLineItemsViewLoads", "validateLineItemsViewLoads",
     "FF_LD_010 — Line Items view loads", "validationUtil.validateLineItemsViewLoads(leftFilterPanelUtil, softAssert);", False),
    ("011", "ValidateLineItemsDynamicGraphql", "validateLineItemsDynamicGraphql",
     "FF_LD_011 — Line Items dynamic GraphQL", "validationUtil.validateLineItemsDynamicGraphql(leftFilterPanelUtil, softAssert);", False),
    ("012", "ValidateMaterialIdInLineItemsGrid", "validateMaterialIdInLineItemsGrid",
     "FF_LD_012 — Material ID in Line Items grid", "validationUtil.validateMaterialIdInLineItemsGrid(softAssert);", True),
    ("013", "ValidateEditCridOnLineItemsRow", "validateEditCridOnLineItemsRow",
     "FF_LD_013 — Edit CRID on Line Items row", "validationUtil.validateEditCridOnLineItemsRow(softAssert);", True),
    ("014", "ValidateOpenTextIdOnLineItemLevel", "validateOpenTextIdOnLineItemLevel",
     "FF_LD_014 — Open Text ID on line item level", "validationUtil.validateOpenTextIdOnLineItemLevel(softAssert);", True),
    ("015", "ValidateLiveFilterCountUpdatesLineItems", "validateLiveFilterCountUpdatesLineItems",
     "FF_LD_015 — Live filter count updates (LI)", "validationUtil.validateLiveFilterCountUpdatesLineItems(leftFilterPanelUtil, softAssert);", False),
    ("016", "ValidateLiveTableUpdatesLineItems", "validateLiveTableUpdatesLineItems",
     "FF_LD_016 — Live table updates (LI)", "validationUtil.validateLiveTableUpdatesLineItems(leftFilterPanelUtil, softAssert);", False),
    ("017", "ValidateRefactoredFilterPanelRenders", "validateRefactoredFilterPanelRenders",
     "FF_LD_017 — Refactored filter panel", "validationUtil.validateRefactoredFilterPanelRenders(leftFilterPanelUtil, softAssert);", False),
    ("018", "ValidateFilterCategoryPagination", "validateFilterCategoryPagination",
     "FF_LD_018 — Filter category pagination", "validationUtil.validateFilterCategoryPagination(leftFilterPanelUtil, softAssert);", False),
    ("019", "ValidateBigFilterCountsBehaviour", "validateBigFilterCountsBehaviour",
     "FF_LD_019 — Big filter counts behaviour", "validationUtil.validateBigFilterCountsBehaviour(leftFilterPanelUtil, softAssert);", False),
    ("020", "ValidateEnhancedDateRangeFiltering", "validateEnhancedDateRangeFiltering",
     "FF_LD_020 — Enhanced date range filtering", "validationUtil.validateEnhancedDateRangeFiltering(softAssert);", False),
    ("021", "ValidateDateRangeCustomization", "validateDateRangeCustomization",
     "FF_LD_021 — Date range customization", "validationUtil.validateDateRangeCustomization(softAssert);", False),
    ("022", "ValidateHistoryLogFunctionality", "validateHistoryLogFunctionality",
     "FF_LD_022 — History log functionality", "validationUtil.validateHistoryLogFunctionality(leftFilterPanelUtil, softAssert);", False),
    ("023", "ValidateAuditLogEntries", "validateAuditLogEntries",
     "FF_LD_023 — Audit log entries", "validationUtil.validateAuditLogEntries(leftFilterPanelUtil, softAssert);", False),
    ("024", "ValidateRevisionWiseAuditLog", "validateRevisionWiseAuditLog",
     "FF_LD_024 — Revision-wise audit log", "validationUtil.validateRevisionWiseAuditLog(leftFilterPanelUtil, softAssert);", False),
    ("025", "ValidateOlderLineItemDifferentiation", "validateOlderLineItemDifferentiation",
     "FF_LD_025 — Older line item differentiation", "validationUtil.validateOlderLineItemDifferentiation(leftFilterPanelUtil, softAssert);", False),
    ("026", "ValidateRevisedStatusesDisplayed", "validateRevisedStatusesDisplayed",
     "FF_LD_026 — Revised statuses displayed", "validationUtil.validateRevisedStatusesDisplayed(leftFilterPanelUtil, softAssert);", False),
    ("027", "ValidateUnifiedHistoryWithApproval", "validateUnifiedHistoryWithApproval",
     "FF_LD_027 — Unified history with approval", "validationUtil.validateUnifiedHistoryWithApproval(leftFilterPanelUtil, softAssert);", False),
    ("028", "ValidateGlimSidecarFilesLoad", "validateGlimSidecarFilesLoad",
     "FF_LD_028 — GLIM sidecar files load", "validationUtil.validateGlimSidecarFilesLoad(softAssert);", False),
    ("029", "ValidateGlimPlayAllFiles", "validateGlimPlayAllFiles",
     "FF_LD_029 — GLIM Play All files", "validationUtil.validateGlimPlayAllFiles(softAssert);", False),
    ("030", "ValidateTestEndpointConnection", "validateTestEndpointConnection",
     "FF_LD_030 — Test endpoint connection", "validationUtil.validateTestEndpointConnection(softAssert);", False),
    ("031", "ValidateEndToEndSmoke", "validateEndToEndSmoke",
     "FF_LD_031 — End-to-end smoke", "validationUtil.validateEndToEndSmoke(leftFilterPanelUtil, softAssert);", False),
    ("032", "ValidateNoConsoleErrorsOnPrimaryViews", "validateNoConsoleErrorsOnPrimaryViews",
     "FF_LD_032 — No console errors on primary views", "validationUtil.validateNoConsoleErrorsOnPrimaryViews(leftFilterPanelUtil, softAssert);", False),
]


def render(num, cls_suffix, method, description, body, needs_li_nav):
    li_nav = ""
    if needs_li_nav:
        li_nav = (
            "        leftFilterPanelUtil.navigateToTab(com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab.LINE_ITEMS);\n"
            "        Thread.sleep(com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterConstants.FILTER_EXPAND_WAIT_MS);\n"
        )
    return f"""package {PKG};

import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.Test;

import static com.paramount.test.ff.uitests.testrail.FeatureFlagTestRailCaseIds.LD_{num};

public class FF_LD_{num}_{cls_suffix} extends FeatureFlagCleanupBaseTest {{

    @Test
    @TmsLink(LD_{num})
    @Description("{description}")
    public void {method}() throws InterruptedException {{
{li_nav}        {body}
        softAssert.assertAll();
    }}
}}
"""


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for num, cls_suffix, method, description, body, needs_li in CASES:
        path = OUT / f"FF_LD_{num}_{cls_suffix}.java"
        path.write_text(render(num, cls_suffix, method, description, body, needs_li), encoding="utf-8")
    print(f"Generated {len(CASES)} test classes in {OUT}")


if __name__ == "__main__":
    main()
