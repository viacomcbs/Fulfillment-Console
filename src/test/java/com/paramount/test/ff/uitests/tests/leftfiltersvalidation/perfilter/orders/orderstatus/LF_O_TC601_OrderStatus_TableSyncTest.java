package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC601 — Order Status table sync (smoke):
 * always select Done: Delivered → table record count matches filter count (or both 0) →
 * first 1–2 visible Status cells show Delivered when count > 0.
 *
 * <p>Run with {@code LF_O_TC601_OrderStatus_TableSync_DevServerSuite.xml} (TestEnvironment=DEV).
 * Running this class directly from IntelliJ uses {@code TestNGSuiteConfig.xml} defaults (PROD).
 */
public class LF_O_TC601_OrderStatus_TableSyncTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC601: Order Status — Done: Delivered count sync + first rows Status = Delivered")
    public void tc601_orderStatus_tableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc601_orderStatus_tableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOrderStatusTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
