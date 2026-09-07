package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.systemname;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC814 — SystemName Scroll. */
public class LF_O_TC814_SystemName_ScrollTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SYSTEM_NAME.getDisplayName();
    private static final int FILTER_INDEX = 14;

    @Test(priority = 1)
    @Description("TC814: SystemName — Scroll")
    public void tc814_systemNameScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc814_systemNameScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
