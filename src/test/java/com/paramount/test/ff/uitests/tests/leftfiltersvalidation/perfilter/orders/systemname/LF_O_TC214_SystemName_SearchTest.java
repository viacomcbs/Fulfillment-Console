package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.systemname;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC214 — SystemName Search. */
public class LF_O_TC214_SystemName_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SYSTEM_NAME.getDisplayName();
    private static final int FILTER_INDEX = 14;

    @Test(priority = 1)
    @Description("TC214: SystemName — Search")
    public void tc214_systemNameSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc214_systemNameSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
