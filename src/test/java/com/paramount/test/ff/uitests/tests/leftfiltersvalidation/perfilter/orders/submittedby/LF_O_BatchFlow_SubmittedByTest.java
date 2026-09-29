package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Submitted By (collapse after search, select all last). */
public class LF_O_BatchFlow_SubmittedByTest extends LeftFilterSubmittedByOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SUBMITTED_BY.getDisplayName();
    private static final int FILTER_INDEX = 5;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Submitted By batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_submittedByAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_submittedByAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
