package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterContinuousFlowRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Continuous flow — all scenarios in one browser session (no refresh between steps). */
public class LF_O_Flow_ActivitytypeTest extends LeftFilterActivityTypeOrdersTabBaseTest {
    private static final int FILTER_INDEX = 20;

    @Test(priority = 1)
    @Description("Activitytype — continuous flow (Basic → Option order → Scroll → Select all → Search → Table sync → Active filters → Clear filters)")
    public void flow_activitytypeAllScenariosContinuous() throws InterruptedException {
        softAssert = new SoftAssert("flow_activitytypeAllScenariosContinuous", getClass().getSimpleName());
        LeftFilterContinuousFlowRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS, FILTER, FILTER_INDEX);
        softAssert.assertAll();
    }
}
