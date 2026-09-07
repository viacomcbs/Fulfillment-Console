package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1019 — DeliveryProtocol Active filters. */
public class LF_O_TC1019_DeliveryProtocol_ActiveFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    private static final int FILTER_INDEX = 19;

    @Test(priority = 1)
    @Description("TC1019: DeliveryProtocol — Active filters")
    public void tc1019_deliveryProtocolActiveFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1019_deliveryProtocolActiveFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.ACTIVE_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
