package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.seasonnumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC610 — Season number order-level table sync (count + Title, Season, Episode S-chip). */
public class LF_O_TC610_SeasonNumber_TableSyncTest extends LeftFilterSeasonNumberOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.SEASON_NUMBER.getDisplayName();

    @Test(priority = 1)
    @Description("TC610: Season number — order-level table sync (count + Title, Season, Episode S-chip)")
    public void tc610_seasonNumberTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc610_seasonNumberTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSeasonNumberTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
