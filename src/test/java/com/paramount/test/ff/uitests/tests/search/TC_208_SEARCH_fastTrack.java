package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 208 | FF-SEARCH-COL-fastTrack: Column search for "Fast Track" (order). */
public class TC_208_SEARCH_fastTrack extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 208;
    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 208)
    @Description("TC-208 FF-SEARCH-COL-fastTrack: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-208_FF-SEARCH-COL-fastTrack");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
