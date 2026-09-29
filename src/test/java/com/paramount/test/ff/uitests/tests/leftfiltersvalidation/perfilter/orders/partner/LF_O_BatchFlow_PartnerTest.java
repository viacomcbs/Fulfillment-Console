package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.partner;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Partner (collapse after search, select all last). */
public class LF_O_BatchFlow_PartnerTest extends LeftFilterPartnerOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.PARTNER.getDisplayName();
    private static final int FILTER_INDEX = 6;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Partner batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_partnerAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_partnerAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
