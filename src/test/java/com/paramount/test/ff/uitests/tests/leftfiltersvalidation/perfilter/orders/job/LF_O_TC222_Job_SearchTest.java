package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.job;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC222 — Job Search. */
public class LF_O_TC222_Job_SearchTest extends LeftFilterJobOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.JOB.getDisplayName();

    @Test(priority = 1)
    @Description("TC222: Job — Search")
    public void tc222_jobSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc222_jobSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchAndSelectFirstOptionForFlow(softAssert, FILTER);
        softAssert.assertAll();
    }
}
