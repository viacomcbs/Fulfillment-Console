package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 237 | FF-SEARCH-COL-NEG-titleSeasonEpisode: Negative column search for "Title, Season, Episode" (order). */
public class TC_237_SEARCH_NEG_titleSeasonEpisode extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 237;
    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 237)
    @Description("TC-237 FF-SEARCH-COL-NEG-titleSeasonEpisode: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-237_FF-SEARCH-COL-NEG-titleSeasonEpisode");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
