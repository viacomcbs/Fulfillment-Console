package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC101 — Order Status Basic: visible, expand, options, Select All, collapse. */
public class LF_O_TC101_OrderStatus_BasicTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC101: Order Status — Basic")
    public void tc101_orderStatus_basic() throws InterruptedException {
        softAssert = new SoftAssert("tc101_orderStatus_basic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
