package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC123 — Job Option list order. */
public class LF_O_TC123_Job_OptionOrderTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();

    @Test(priority = 1)
    @Description("TC123: Job — Option list order")
    public void tc123_jobOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc123_jobOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
