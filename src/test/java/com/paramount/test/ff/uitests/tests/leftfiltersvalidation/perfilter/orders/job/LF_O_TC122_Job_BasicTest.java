package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC122 — Job Basic. */
public class LF_O_TC122_Job_BasicTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();
    private static final int FILTER_INDEX = 22;

    @Test(priority = 1)
    @Description("TC122: Job — Basic")
    public void tc122_jobBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc122_jobBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
