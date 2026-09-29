package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 127 | FF-SORT-COL-titleSeasonEpisode-ASC: Sort "Title, Season, Episode" ASC (order). */
public class TC_127_SORT_titleSeasonEpisode_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 127;
    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 127)
    @Description("TC-127 FF-SORT-COL-titleSeasonEpisode-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-127_FF-SORT-COL-titleSeasonEpisode-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
