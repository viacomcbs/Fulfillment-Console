package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.orders.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.OrdersLeftFilter;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC612 — Episode number order-level table sync (count + Title, Season, Episode column). */
public class LF_O_TC612_EpisodeNumber_TableSyncTest extends LeftFilterEpisodeNumberOrdersTabBaseTest {

    private static final String FILTER = OrdersLeftFilter.EPISODE_NUMBER.getDisplayName();

    @Test(priority = 1)
    @Description("TC612: Episode number — order-level table sync (count + Title, Season, Episode)")
    public void tc612_episodeNumberTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc612_episodeNumberTableSync", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.ORDERS);
        leftFilterPanelUtil.validateEpisodeNumberTableSyncSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
