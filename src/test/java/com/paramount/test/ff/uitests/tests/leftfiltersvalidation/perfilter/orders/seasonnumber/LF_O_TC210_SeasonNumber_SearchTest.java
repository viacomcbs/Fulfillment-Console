package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC210 — SeasonNumber Search (N/A for range filter — skipped via {@link LeftFilterPerFilterTestRunner}). */
public class LF_O_TC210_SeasonNumber_SearchTest extends LeftFilterSeasonNumberOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SEASON_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 10;

    @Test(priority = 1)
    @Description("TC210: SeasonNumber — Search")
    public void tc210_seasonNumberSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc210_seasonNumberSearch", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.ORDERS,
                FILTER, LeftFilterTestCategory.SEARCH, FILTER_INDEX);
        softAssert.assertAll();
    }
}
