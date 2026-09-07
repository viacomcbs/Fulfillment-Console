package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC102 — Order Status option list order (first visible viewport):
 * Select All first → non-zero A-Z → divider → zero-count A-Z.
 */
public class LF_O_TC102_OrderStatus_OptionOrderTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();

    @Test(priority = 1)
    @Description("TC102: Order Status — option list order (Select All, non-zero A-Z, divider, zero A-Z)")
    public void tc102_orderStatus_optionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc102_orderStatus_optionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
