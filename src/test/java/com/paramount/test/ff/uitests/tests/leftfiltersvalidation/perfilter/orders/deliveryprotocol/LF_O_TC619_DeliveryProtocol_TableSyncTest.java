package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.deliveryprotocol;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * TC619 — Delivery Protocol table sync on Orders view (Demand system-style count sync +
 * package-level Endpoint Info validation):
 * filter count match → expand order → click package → Endpoint Info tab → Delivery Protocol matches.
 */
public class LF_O_TC619_DeliveryProtocol_TableSyncTest extends LeftFilterDeliveryProtocolOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DELIVERY_PROTOCOL.getDisplayName();

    @Test(priority = 1)
    @Description("TC619: Delivery Protocol — count sync + package Endpoint Info Delivery Protocol matches filter")
    public void tc619_deliveryProtocolTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc619_deliveryProtocolTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateDeliveryProtocolTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
