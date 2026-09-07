package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;

import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1001 — Order Status active filter chips. */
public class LF_O_TC1001_OrderStatus_ActiveFiltersTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Test(priority = 1)
    @Description("TC1001: Order Status — Active filters")
    public void tc1001_orderStatus_activeFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1001_orderStatus_activeFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
