package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.lineitemstatus.LeftFilterLineItemStatusOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC601 — Line Item Status table sync on Orders view.
 *
 * <ol>
 *   <li>Login + Yesterday calendar (suite {@code @BeforeMethod})</li>
 *   <li>Line Item Status left filter — first option with count &gt; 0</li>
 *   <li>Expand order → wait for line-item grid → verify at least one Line Item Status matches filter</li>
 *   <li>Filter count vs table record count is not checked on Orders view</li>
 * </ol>
 */
public class LF_O_TC601_LineItemStatus_TableSyncTest extends LeftFilterLineItemStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.LINE_ITEM_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC601: Line Item Status — expand order row; at least one line item status matches filter")
    public void tc601_lineItemStatusTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc601_lineItemStatusTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateLineItemStatusTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
