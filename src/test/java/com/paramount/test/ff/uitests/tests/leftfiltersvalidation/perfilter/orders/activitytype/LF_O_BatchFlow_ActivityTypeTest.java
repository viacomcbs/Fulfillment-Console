package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterBatchFlowRunner;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Batch flow — Activity Type (collapse after search, select all last). */
public class LF_O_BatchFlow_ActivityTypeTest extends LeftFilterActivityTypeOrdersTabBaseTest {

    private static final int FILTER_INDEX = 20;

    @Override
    protected boolean requiresAutomationViewSetup() {
        return false;
    }

    @Test(priority = 1)
    @Description("Activity Type batch flow — Basic → Option order → Scroll → Search → Collapse → Table sync → Active → Clear → Select all")
    public void batchFlow_activityTypeAllScenarios() throws InterruptedException {
        softAssert = new SoftAssert("batchFlow_activityTypeAllScenarios", getClass().getSimpleName());
        LeftFilterBatchFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
