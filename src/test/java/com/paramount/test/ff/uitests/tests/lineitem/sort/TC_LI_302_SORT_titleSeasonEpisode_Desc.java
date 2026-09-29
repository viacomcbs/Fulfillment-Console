package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-302 | FF-LI-SORT-COL-titleSeasonEpisode-DESC: Sort "Title, Season, Episode" descending on Line Items tab. */
public class TC_LI_302_SORT_titleSeasonEpisode_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 302)
    @Description("TC-LI-302 FF-LI-SORT-COL-titleSeasonEpisode-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-302_FF-LI-SORT-COL-titleSeasonEpisode-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
