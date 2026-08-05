package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.flag;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC108 — Flag Basic. */
public class LF_O_TC108_Flag_BasicTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.FLAG.getDisplayName();
    private static final int FILTER_INDEX = 8;

    @Test(priority = 1)
    @Description("TC108: Flag — Basic")
    public void tc108_flagBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc108_flagBasic", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateBasicSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
