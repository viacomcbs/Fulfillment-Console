package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC212 — EpisodeNumber Search. */
public class LF_LI_TC212_EpisodeNumber_SearchTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.EPISODE_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 12;

    @Test(priority = 1)
    @Description("TC212: EpisodeNumber — Search")
    public void tc212_episodeNumberSearch() throws InterruptedException {
        softAssert = new SoftAssert("tc212_episodeNumberSearch", getClass().getSimpleName());
        leftFilterPanelUtil.navigateToTab(ConsoleTab.LINE_ITEMS);
        leftFilterPanelUtil.validateSearchSmoke(softAssert, FILTER);
        softAssert.assertAll();
    }
}
