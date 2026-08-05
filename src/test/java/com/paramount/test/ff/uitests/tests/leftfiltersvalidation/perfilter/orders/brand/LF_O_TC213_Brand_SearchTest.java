package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC213 — Brand Search. */
public class LF_O_TC213_Brand_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.BRAND.getDisplayName();
    private static final int FILTER_INDEX = 13;

    @Test(priority = 1)
    @Description("TC213: Brand — Search")
    public void tc213_brandSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc213_brandSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
