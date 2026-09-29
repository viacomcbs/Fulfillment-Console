package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/**
 * Batch prep for Environment &amp; routing group — no Manage columns changes required
 * (Environment, Region, System Name, Demand system, Delivery Protocol use count-only table sync).
 */
public class LF_O_Batch5Filters_EnvironmentRouting_ManageColumnsSetupTest extends LeftFilterOrdersTabBaseTest {

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Batch setup — confirm Orders tab ready (no Manage columns for routing filters)")
    public void setup_environmentRoutingBatchReady() throws InterruptedException {
        softAssert = new SoftAssert("setup_environmentRoutingBatchReady", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        Logger.logReportMessage("Environment & routing batch — no Manage columns setup required; using default Orders grid");
        softAssert.assertAll();
    }
}
