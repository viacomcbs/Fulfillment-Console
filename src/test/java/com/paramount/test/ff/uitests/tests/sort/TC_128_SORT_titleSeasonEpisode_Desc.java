package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 128 | FF-SORT-COL-titleSeasonEpisode-DESC: Sort "Title, Season, Episode" DESC (order). */
public class TC_128_SORT_titleSeasonEpisode_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 128;
    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 128)
    @Description("TC-128 FF-SORT-COL-titleSeasonEpisode-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-128_FF-SORT-COL-titleSeasonEpisode-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
