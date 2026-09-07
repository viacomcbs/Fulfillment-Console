package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.submittedby.LeftFilterSubmittedByOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC105 — SubmittedBy Basic. */
public class LF_O_TC105_SubmittedBy_BasicTest extends LeftFilterSubmittedByOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SUBMITTED_BY.getDisplayName();
    private static final int FILTER_INDEX = 5;

    @Test(priority = 1)
    @Description("TC105: SubmittedBy — Basic")
    public void tc105_submittedByBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc105_submittedByBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
