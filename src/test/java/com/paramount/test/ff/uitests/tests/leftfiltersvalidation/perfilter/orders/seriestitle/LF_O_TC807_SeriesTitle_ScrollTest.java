package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seriestitle;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC807 — SeriesTitle Scroll. */
public class LF_O_TC807_SeriesTitle_ScrollTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SERIES_TITLE.getDisplayName();
    private static final int FILTER_INDEX = 7;

    @Test(priority = 1)
    @Description("TC807: SeriesTitle — Scroll")
    public void tc807_seriesTitleScroll() throws InterruptedException {
        softAssert = new SoftAssert("tc807_seriesTitleScroll", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SCROLL, FILTER_INDEX);
        softAssert.assertAll();
    }
}
