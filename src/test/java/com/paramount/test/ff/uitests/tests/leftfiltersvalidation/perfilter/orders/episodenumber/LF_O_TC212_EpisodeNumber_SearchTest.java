package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.orders.LeftFilterOrdersTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC212 — EpisodeNumber Search. */
public class LF_O_TC212_EpisodeNumber_SearchTest extends LeftFilterOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.EPISODE_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 12;

    @Test(priority = 1)
    @Description("TC212: EpisodeNumber — Search")
    public void tc212_episodeNumberSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc212_episodeNumberSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
