package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 187 | FF-SEARCH-COL-episode: Column search for "Episode" (order). */
public class TC_187_SEARCH_episode extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 187;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 187)
    @Description("TC-187 FF-SEARCH-COL-episode: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-187_FF-SEARCH-COL-episode");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
