package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.orderstatus;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Order Status (collapse after search, select all last). */
public class LF_O_BatchFlow_OrderStatusTest extends LeftFilterOrderStatusOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ORDER_STATUS.getDisplayName();
    private static final int FILTER_INDEX = 1;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Order Status batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_orderStatusAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_orderStatusAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
