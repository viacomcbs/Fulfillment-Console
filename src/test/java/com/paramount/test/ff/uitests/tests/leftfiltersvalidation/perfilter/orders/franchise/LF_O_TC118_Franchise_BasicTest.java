package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.franchise;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC118 — Franchise Basic. */
public class LF_O_TC118_Franchise_BasicTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FRANCHISE.getDisplayName();
    private static final int FILTER_INDEX = 18;

    @Test(priority = 1)
    @Description("TC118: Franchise — Basic")
    public void tc118_franchiseBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc118_franchiseBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
