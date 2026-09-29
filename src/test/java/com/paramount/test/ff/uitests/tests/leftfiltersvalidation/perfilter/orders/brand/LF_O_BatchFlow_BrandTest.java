package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Brand (collapse after search, select all last). */
public class LF_O_BatchFlow_BrandTest extends LeftFilterBrandOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.BRAND.getDisplayName();
    private static final int FILTER_INDEX = 13;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Brand batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_brandAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_brandAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
