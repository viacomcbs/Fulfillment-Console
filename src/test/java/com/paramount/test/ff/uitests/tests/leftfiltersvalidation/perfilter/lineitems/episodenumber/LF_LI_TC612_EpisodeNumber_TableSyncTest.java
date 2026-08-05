package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC612 — EpisodeNumber Table sync. */
public class LF_LI_TC612_EpisodeNumber_TableSyncTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.EPISODE_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 12;

    @Test(priority = 1)
    @Description("TC612: EpisodeNumber — Table sync")
    public void tc612_episodeNumberTableSync() throws InterruptedException {
        softAssert = new SoftAssert("tc612_episodeNumberTableSync", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.TABLE_SYNC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
