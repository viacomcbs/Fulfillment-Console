package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC819 — DeliveryProtocol Scroll. */
public class LF_O_TC819_DeliveryProtocol_ScrollTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    private static final int FILTER_INDEX = 19;

    @Test(priority = 1)
    @Description("TC819: DeliveryProtocol — Scroll")
    public void tc819_deliveryProtocolScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc819_deliveryProtocolScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
