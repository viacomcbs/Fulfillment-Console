package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.brand.LeftFilterBrandOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC813 — Brand Scroll. */
public class LF_O_TC813_Brand_ScrollTest extends LeftFilterBrandOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.BRAND.getDisplayName();
    private static final int FILTER_INDEX = 13;

    @Test(priority = 1)
    @Description("TC813: Brand — Scroll")
    public void tc813_brandScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc813_brandScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
