package com.paramount.test.ff.uitests.base;

import com.paramount.test.ff.common.loginUtil.Verify;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.helpers.FilterPanel_Util;
import com.paramount.test.ff.uitests.helpers.FulfillmentJsUtil;
import com.paramount.test.ff.uitests.helpers.GridSort_util;
import com.paramount.test.ff.uitests.helpers.LineItemTabColumnData;

/**
 * Base for Line Items tab per-column sort tests (ascending / descending).
 */
public abstract class LineItemTabSortBaseTest extends ManageColumnsLineItemBaseTest {

    private static boolean lineItemSortSuitePrepared;

    protected GridSort_util gridSortUtil;

    @Override
    protected void ensureHelpersInitialized() {
        super.ensureHelpersInitialized();
        if (gridSortUtil == null) {
            gridSortUtil = new GridSort_util();
        }
    }

    protected void prepareLineItemSortSuiteOnce() throws InterruptedException {
        ensureHelpersInitialized();
        if (lineItemSortSuitePrepared) {
            lineItemTabUtil.navigateToLineItemsTab();
            lineItemTabUtil.waitForLineItemsGridReadyFast(5);
            return;
        }
        Logger.logReportMessage("Line Items sort suite: login, Yesterday filter, Line Items grid ready");
        launchFulfillmentConsoleAndOpenLineItemsTab();
        if (!FilterPanel_Util.isYesterdayDateFilterCached()) {
            filterPanelUtil.applyYesterdayDateFilter();
        }
        lineItemTabUtil.navigateToLineItemsTab();
        lineItemTabUtil.waitForLineItemsGridReadyFast(15);
        lineItemSortSuitePrepared = true;
    }

    protected void runLineItemSortAscendingTest(String columnName, String columnId, String valueType)
            throws InterruptedException {
        prepareLineItemSortSuiteOnce();
        String effectiveId = resolveLineItemColumnId(columnName, columnId);
        if (!ensureLineItemColumnVisibleForSort(columnName, effectiveId)) {
            Verify.softAssert(false, columnName + " could not be prepared for sort on Line Items tab");
            return;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, effectiveId);
        gridSortUtil.sortAscending(effectiveId, columnName);
        Thread.sleep(200);
        lineItemTabUtil.waitForLineItemsGridReadyFast(5);
        gridSortUtil.verifySortedAscending(columnName, effectiveId, valueType, 2);
    }

    protected void runLineItemSortDescendingTest(String columnName, String columnId, String valueType)
            throws InterruptedException {
        prepareLineItemSortSuiteOnce();
        String effectiveId = resolveLineItemColumnId(columnName, columnId);
        if (!ensureLineItemColumnVisibleForSort(columnName, effectiveId)) {
            Verify.softAssert(false, columnName + " could not be prepared for sort on Line Items tab");
            return;
        }
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, effectiveId);
        gridSortUtil.sortDescending(effectiveId, columnName);
        Thread.sleep(200);
        lineItemTabUtil.waitForLineItemsGridReadyFast(5);
        gridSortUtil.verifySortedDescending(columnName, effectiveId, valueType, 2);
    }

    private boolean ensureLineItemColumnVisibleForSort(String columnName, String columnId)
            throws InterruptedException {
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        if (gridSortUtil.isSortableHeaderVisible(columnId, columnName)) {
            return true;
        }
        if (FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId)
                && gridSortUtil.isSortableHeaderVisible(columnId, columnName)) {
            return true;
        }
        lineItemTabUtil.closeManageColumnsIfOpen();
        lineItemTabUtil.openManageColumns();
        lineItemTabUtil.selectStandardView();
        if (!lineItemTabUtil.isColumnListed(columnName)) {
            lineItemTabUtil.closeManageColumns();
            return false;
        }
        lineItemTabUtil.enableColumn(columnName);
        lineItemTabUtil.closeManageColumns();
        lineItemTabUtil.refreshLineItemsGridAfterColumnChange();
        FulfillmentJsUtil.scrollColumnHeaderIntoView(columnName, columnId);
        return gridSortUtil.isSortableHeaderVisible(columnId, columnName)
                || FulfillmentJsUtil.isLineItemTabGridColumnVisible(columnName, columnId);
    }

    private String resolveLineItemColumnId(String columnName, String columnId) {
        if (columnId != null && !columnId.isEmpty()) {
            return columnId;
        }
        LineItemTabColumnData.ColumnDef column = LineItemTabColumnData.findByLabel(columnName);
        return column == null ? columnName.replaceAll("[^A-Za-z0-9]+", "") : column.id;
    }

    public static void resetLineItemSortSuiteState() {
        lineItemSortSuitePrepared = false;
        resetLineItemTabSession();
    }
}
