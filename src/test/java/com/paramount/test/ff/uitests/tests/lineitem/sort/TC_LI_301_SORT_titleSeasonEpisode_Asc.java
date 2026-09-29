package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-301 | FF-LI-SORT-COL-titleSeasonEpisode-ASC: Sort "Title, Season, Episode" ascending on Line Items tab. */
public class TC_LI_301_SORT_titleSeasonEpisode_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 301)
    @Description("TC-LI-301 FF-LI-SORT-COL-titleSeasonEpisode-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-301_FF-LI-SORT-COL-titleSeasonEpisode-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
