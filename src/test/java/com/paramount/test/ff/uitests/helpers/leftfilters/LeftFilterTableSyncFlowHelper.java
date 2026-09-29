package com.paramount.test.ff.uitests.helpers.leftfilters;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.managecolumns.ManageColumnOptions;

/**
 * Table-sync step for continuous flow — preserves each filter's dedicated validation logic.
 */
public final class LeftFilterTableSyncFlowHelper {

    private LeftFilterTableSyncFlowHelper() {
    }

    public static void runForSelectedOption(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, ConsoleTab tab,
                                            String filterName, String selectedOption,
                                            LeftFilterPerFilterConfig.FilterSpec spec) throws InterruptedException {
        Logger.logMessage(filterName + " table sync (flow) using search-selected option: " + selectedOption);

        if (OrdersLeftFilter.SUBMITTED_BY.getDisplayName().equals(filterName)) {
            panelUtil.validateSubmittedByTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.ASSIGNED_TO.getDisplayName().equals(filterName)) {
            panelUtil.validateAssignedToTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName().equals(filterName)) {
            panelUtil.validateDemandSystemTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName().equals(filterName)) {
            panelUtil.validateDeliveryProtocolTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.LANGUAGE.getDisplayName().equals(filterName)) {
            panelUtil.validateLanguageTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.REGION.getDisplayName().equals(filterName)) {
            panelUtil.validateRegionTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.PARTNER.getDisplayName().equals(filterName)) {
            panelUtil.validatePartnerTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName().equals(filterName)) {
            panelUtil.validateActivityTypeTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (OrdersLeftFilter.JOB.getDisplayName().equals(filterName)
                || OrdersLeftFilter.JOB_TYPE.getDisplayName().equals(filterName)) {
            panelUtil.validateJobTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else if (isCountOnlyFilter(filterName)) {
            panelUtil.validateCountOnlyTableSyncForSelectedOption(softAssert, filterName, selectedOption);
        } else {
            String tableColumn = spec.getTableColumn();
            if (tableColumn != null && !tableColumn.isBlank()
                    && !tableColumn.equalsIgnoreCase(filterName)
                    && !ManageColumnOptions.TITLE_SEASON_EPISODE.equals(tableColumn)) {
                panelUtil.validateOrderLevelColumnTableSyncForSelectedOption(softAssert, filterName, selectedOption,
                        tableColumn);
            } else {
                panelUtil.validateCountOnlyTableSyncForSelectedOption(softAssert, filterName, selectedOption);
            }
        }
    }

    public static void runStandalone(SoftAssert softAssert, LeftFilterPanelUtil panelUtil, String filterName)
            throws InterruptedException {
        Logger.logMessage(filterName + " table sync (flow) standalone");

        if (OrdersLeftFilter.SUBMITTED_BY.getDisplayName().equals(filterName)) {
            panelUtil.validateSubmittedByTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.ASSIGNED_TO.getDisplayName().equals(filterName)) {
            panelUtil.validateAssignedToTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName().equals(filterName)) {
            panelUtil.validateDemandSystemTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName().equals(filterName)) {
            panelUtil.validateDeliveryProtocolTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.LANGUAGE.getDisplayName().equals(filterName)) {
            panelUtil.validateLanguageTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.REGION.getDisplayName().equals(filterName)) {
            panelUtil.validateRegionTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.PARTNER.getDisplayName().equals(filterName)) {
            panelUtil.validatePartnerTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.FLAG.getDisplayName().equals(filterName)) {
            panelUtil.validateFlagTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName().equals(filterName)) {
            panelUtil.validateActivityTypeTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.CONTENT_TYPE.getDisplayName().equals(filterName)) {
            panelUtil.validateContentTypeTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.BRAND.getDisplayName().equals(filterName)) {
            panelUtil.validateBrandTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName().equals(filterName)) {
            panelUtil.validateLineItemStatusTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.JOB.getDisplayName().equals(filterName)
                || OrdersLeftFilter.JOB_TYPE.getDisplayName().equals(filterName)) {
            panelUtil.validateJobTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.SERIES_TITLE.getDisplayName().equals(filterName)) {
            panelUtil.validateSeriesTitleTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.SEASON_NUMBER.getDisplayName().equals(filterName)) {
            panelUtil.validateSeasonNumberTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.EPISODE_NUMBER.getDisplayName().equals(filterName)) {
            panelUtil.validateEpisodeNumberTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.ORDER_STATUS.getDisplayName().equals(filterName)) {
            panelUtil.validateOrderStatusTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.ENVIRONMENT.getDisplayName().equals(filterName)) {
            panelUtil.validateEnvironmentTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.FRANCHISE.getDisplayName().equals(filterName)) {
            panelUtil.validateFranchiseTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.SYSTEM_NAME.getDisplayName().equals(filterName)) {
            panelUtil.validateSystemNameTableSyncSmoke(softAssert, filterName);
        } else if (OrdersLeftFilter.ERROR_MESSAGE.getDisplayName().equals(filterName)) {
            panelUtil.validateOrderLevelColumnTableSyncSmoke(softAssert, filterName,
                    ManageColumnOptions.ERROR_MESSAGES);
        } else {
            LeftFilterPerFilterConfig.FilterSpec spec =
                    LeftFilterPerFilterConfig.specFor(ConsoleTab.ORDERS, filterName);
            LeftFilterPerFilterTestRunner.runTableSyncOnly(softAssert, panelUtil, filterName, spec);
        }
    }

    private static boolean isCountOnlyFilter(String filterName) {
        return OrdersLeftFilter.ORDER_STATUS.getDisplayName().equals(filterName)
                || OrdersLeftFilter.ENVIRONMENT.getDisplayName().equals(filterName)
                || OrdersLeftFilter.FRANCHISE.getDisplayName().equals(filterName)
                || OrdersLeftFilter.SYSTEM_NAME.getDisplayName().equals(filterName);
    }
}
