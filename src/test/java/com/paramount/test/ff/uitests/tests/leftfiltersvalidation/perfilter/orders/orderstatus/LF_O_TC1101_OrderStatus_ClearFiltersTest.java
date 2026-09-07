package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1101 — Order Status clear filters. */
public class LF_O_TC1101_OrderStatus_ClearFiltersTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC1101: Order Status — Clear filters")
    public void tc1101_orderStatus_clearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1101_orderStatus_clearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
