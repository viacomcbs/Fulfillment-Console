package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC208 — Flag Search. */
public class LF_O_TC208_Flag_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FLAG.getDisplayName();
    private static final int FILTER_INDEX = 8;

    @Test(priority = 1)
    @Description("TC208: Flag — Search")
    public void tc208_flagSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc208_flagSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
