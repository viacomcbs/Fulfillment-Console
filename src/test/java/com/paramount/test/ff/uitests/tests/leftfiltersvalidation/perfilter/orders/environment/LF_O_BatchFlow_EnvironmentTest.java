package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.environment;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Environment (collapse after search, select all last). */
public class LF_O_BatchFlow_EnvironmentTest extends LeftFilterEnvironmentOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ENVIRONMENT.getDisplayName();
    private static final int FILTER_INDEX = 3;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Environment batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_environmentAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_environmentAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
