package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.contenttype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Content type (collapse after search, select all last). */
public class LF_O_BatchFlow_ContentTypeTest extends LeftFilterContentTypeOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.CONTENT_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 21;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Content type batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_contentTypeAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_contentTypeAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
