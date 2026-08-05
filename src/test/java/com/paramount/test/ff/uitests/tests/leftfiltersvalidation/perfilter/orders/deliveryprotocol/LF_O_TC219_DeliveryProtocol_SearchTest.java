package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC219 — DeliveryProtocol Search. */
public class LF_O_TC219_DeliveryProtocol_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();
    private static final int FILTER_INDEX = 19;

    @Test(priority = 1)
    @Description("TC219: DeliveryProtocol — Search")
    public void tc219_deliveryProtocolSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc219_deliveryProtocolSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
