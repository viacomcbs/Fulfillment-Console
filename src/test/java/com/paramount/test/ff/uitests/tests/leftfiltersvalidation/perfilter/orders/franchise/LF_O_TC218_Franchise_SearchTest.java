package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.franchise;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC218 — Franchise Search. */
public class LF_O_TC218_Franchise_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FRANCHISE.getDisplayName();
    private static final int FILTER_INDEX = 18;

    @Test(priority = 1)
    @Description("TC218: Franchise — Search")
    public void tc218_franchiseSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc218_franchiseSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
