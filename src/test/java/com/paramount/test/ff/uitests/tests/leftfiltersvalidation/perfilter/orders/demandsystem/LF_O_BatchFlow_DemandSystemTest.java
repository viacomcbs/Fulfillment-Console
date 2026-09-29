package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Demand system (collapse after search, select all last). */
public class LF_O_BatchFlow_DemandSystemTest extends LeftFilterDemandSystemOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName();
    private static final int FILTER_INDEX = 16;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Demand system batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_demandSystemAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_demandSystemAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
