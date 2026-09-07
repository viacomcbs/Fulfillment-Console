package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1119 — DeliveryProtocol Clear filters. */
public class LF_O_TC1119_DeliveryProtocol_ClearFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    private static final int FILTER_INDEX = 19;

    @Test(priority = 1)
    @Description("TC1119: DeliveryProtocol — Clear filters")
    public void tc1119_deliveryProtocolClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1119_deliveryProtocolClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
