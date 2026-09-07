package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.lineitemstatus.LeftFilterLineItemStatusLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC601 — Line Item Status table sync on Line Items view.
 * Reads {@code td.revised-status-col} / {@code span.status-label} on the main grid (no row expand).
 * At least one visible status must match the selected filter option.
 */
public class LF_LI_TC601_LineItemStatus_TableSyncTest extends LeftFilterLineItemStatusLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.LINE_ITEM_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC601: Line Item Status — filter count sync; at least one grid status matches filter")
    public void tc601_lineItemStatusTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc601_lineItemStatusTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateLineItemStatusTableSyncOnLineItemsTab(softAssert, FILTER);
        softAssert.assertAll();
    }
}
