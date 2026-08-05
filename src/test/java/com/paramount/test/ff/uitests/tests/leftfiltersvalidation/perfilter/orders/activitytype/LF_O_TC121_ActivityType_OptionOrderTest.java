package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.activitytype;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC121 — ActivityType Option list order. */
public class LF_O_TC121_ActivityType_OptionOrderTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.ACTIVITY_TYPE.getDisplayName();
    private static final int FILTER_INDEX = 20;

    @Test(priority = 1)
    @Description("TC121: ActivityType — Option list order")
    public void tc121_activityTypeOptionOrder() throws InterruptedException {
        softAssert = new SoftAssert("tc121_activityTypeOptionOrder", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateOptionListOrderSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
