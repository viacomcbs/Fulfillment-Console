package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seriestitle;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC1107 — SeriesTitle Clear filters. */
public class LF_O_TC1107_SeriesTitle_ClearFiltersTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SERIES_TITLE.getDisplayName();
    private static final int FILTER_INDEX = 7;

    @Test(priority = 1)
    @Description("TC1107: SeriesTitle — Clear filters")
    public void tc1107_seriesTitleClearFilters() throws InterruptedException {
        softAssert = new SoftAssert("tc1107_seriesTitleClearFilters", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.CLEAR_FILTERS, FILTER_INDEX);
        softAssert.assertAll();
    }
}
