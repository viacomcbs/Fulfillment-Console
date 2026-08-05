package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.demandsystem;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC216 — DemandSystem Search. */
public class LF_O_TC216_DemandSystem_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.DEMAND_SYSTEM.getDisplayName();
    private static final int FILTER_INDEX = 16;

    @Test(priority = 1)
    @Description("TC216: DemandSystem — Search")
    public void tc216_demandSystemSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc216_demandSystemSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
