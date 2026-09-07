package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC601 — Order Status table sync on Orders view (Brand-style):
 * first non-zero option → filter count = table record count →
 * every visible Status column cell matches the mapped table keyword.
 */
public class LF_O_TC601_OrderStatus_TableSyncTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC601: Order Status — filter count matches table; all visible Status cells match filter")
    public void tc601_orderStatus_tableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc601_orderStatus_tableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOrderStatusTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
