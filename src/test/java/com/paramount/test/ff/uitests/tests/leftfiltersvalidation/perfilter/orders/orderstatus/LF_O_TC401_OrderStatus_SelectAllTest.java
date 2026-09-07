package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC401 — Order Status Select All. Enable when ready to run. */
public class LF_O_TC401_OrderStatus_SelectAllTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC401: Order Status — Select all")
    public void tc401_orderStatus_selectAll() throws InterruptedException {
        softAssert = new SoftAssert("tc401_orderStatus_selectAll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SELECT_ALL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
