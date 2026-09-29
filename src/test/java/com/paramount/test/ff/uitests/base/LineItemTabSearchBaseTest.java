package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.GridColumnSearch_util;
import com.paramount.test.ff.uitests.helpers.LineItemTabColumnData;

/**
 * Base for Line Items tab per-column search / filter tests.
 */
public abstract class LineItemTabSearchBaseTest extends ManageColumnsLineItemBaseTest {

    private static boolean lineItemSearchSuitePrepared;

    protected GridColumnSearch_util gridSearchUtil;

    @Override
    protected void ensureHelpersInitialized() {
        super.ensureHelpersInitialized();
        if (gridSearchUtil == null) {
            gridSearchUtil = new GridColumnSearch_util();
        }
    }

    protected void prepareLineItemSearchSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        if (lineItemSearchSuitePrepared) {
            lineItemTabUtil.navigateToLineItemsTab();
            lineItemTabUtil.waitForLineItemsGridReadyFast(5);
            return;
        }
        Logger.logReportMessage("Line Items search suite: login, Yesterday filter, Line Items grid ready");
        launchFulfillmentConsoleAndOpenLineItemsTab();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        lineItemTabUtil.navigateToLineItemsTab();
        lineItemTabUtil.waitForLineItemsGridReadyFast(15);
        lineItemSearchSuitePrepared = true;
    }

    protected void runLineItemColumnSearchTest(
            String columnName, String columnId, boolean searchable, String searchType)
            throws InterruptedException {
        prepareLineItemSearchSuiteOnce();
        String effectiveId = resolveLineItemColumnId(columnName, columnId);
        if (!ensureLineItemColumnVisibleForSearch(columnName, effectiveId)) {
            Verify.softAssert(false, columnName + " could not be prepared for search on Line Items tab");
            return;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, effectiveId);
        if (searchable) {
            if ("DATE".equalsIgnoreCase(searchType)) {
                gridSearchUtil.verifySearchableDateColumn(columnName, effectiveId);
            } else {
                gridSearchUtil.verifySearchableTextColumn(columnName, effectiveId);
            }
        } else {
            gridSearchUtil.verifyNonSearchableColumn(columnName, effectiveId);
        }
    }

    private boolean ensureLineItemColumnVisibleForSearch(String columnName, String columnId)
            throws InterruptedException {
        if (FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId)) {
            return true;
        }
        lineItemTabUtil.closeManageColumnsIfOpen();
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.selectStandardView();
        if (!lineItemTabUtil.isColumnListed(columnName)) {
            Logger.log("Column not listed in Line item table Manage Columns: " + columnName);
            lineItemTabUtil.closeManageColumns();
            return false;
        }
        lineItemTabUtil.enableColumn(columnName);
        lineItemTabUtil.closeManageColumns();
        lineItemTabUtil.refreshLineItemsGridAfterColumnChange();
        return FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId);
    }

    private String resolveLineItemColumnId(String columnName, String columnId) {
        if (columnId != null && !columnId.isEmpty()) {
            return columnId;
        }
        LineItemTabColumnData.ColumnDef column = LineItemTabColumnData.findByLabel(columnName);
        return column == null ? columnName.replaceAll("[^A-Za-z0-9]+", "") : column.id;
    }

    public static void resetLineItemSearchSuiteState() {
        lineItemSearchSuitePrepared = false;
        resetLineItemTabSession();
    }
}
