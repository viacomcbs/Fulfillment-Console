package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 238 | FF-SEARCH-COL-NEG-episode: Negative column search for "Episode" (order). */
public class TC_238_SEARCH_NEG_episode extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 238;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 238)
    @Description("TC-238 FF-SEARCH-COL-NEG-episode: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-238_FF-SEARCH-COL-NEG-episode");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
