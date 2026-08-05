package com.paramount.test.ff.uitests.tests.leftfiltersvalidation.perfilter.lineitems.episodenumber;

import com.paramount.test.ff.common.util.SoftAssert;
import com.paramount.test.ff.uitests.helpers.leftfilters.ConsoleTab;
import com.paramount.test.ff.uitests.helpers.leftfilters.LineItemsLeftFilter;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterPerFilterTestRunner;
import com.paramount.test.ff.uitests.helpers.leftfilters.LeftFilterTestCategory;
import com.paramount.test.ff.uitests.tests.leftfiltersvalidation.lineitems.LeftFilterLineItemsTabBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC112 — EpisodeNumber Basic. */
public class LF_LI_TC112_EpisodeNumber_BasicTest extends LeftFilterLineItemsTabBaseTest {

    private static final String FILTER = LineItemsLeftFilter.EPISODE_NUMBER.getDisplayName();
    private static final int FILTER_INDEX = 12;

    @Test(priority = 1)
    @Description("TC112: EpisodeNumber — Basic")
    public void tc112_episodeNumberBasic() throws InterruptedException {
        softAssert = new SoftAssert("tc112_episodeNumberBasic", getClass().getSimpleName());
        LeftFilterPerFilterTestRunner.run(softAssert, leftFilterPanelUtil, ConsoleTab.LINE_ITEMS,
                FILTER, LeftFilterTestCategory.BASIC, FILTER_INDEX);
        softAssert.assertAll();
    }
}
