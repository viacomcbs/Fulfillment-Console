package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 246 | FF-SEARCH-COL-NEG-episodeName: Negative column search for "Episode name" (order). */
public class TC_246_SEARCH_NEG_episodeName extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 246;
    private static final String COLUMN_ID = "episodeName";
    private static final String COLUMN_NAME = "Episode name";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 246)
    @Description("TC-246 FF-SEARCH-COL-NEG-episodeName: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-246_FF-SEARCH-COL-NEG-episodeName");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
