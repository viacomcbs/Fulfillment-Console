package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby.LeftFilterSubmittedByOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC106 — SubmittedBy Option list order. */
public class LF_O_TC106_SubmittedBy_OptionOrderTest extends LeftFilterSubmittedByOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SUBMITTED_BY.getDisplayName();
    private static final int FILTER_INDEX = 5;

    @Test(priority = 1)
    @Description("TC106: SubmittedBy — Option list order")
    public void tc106_submittedByOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc106_submittedByOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
