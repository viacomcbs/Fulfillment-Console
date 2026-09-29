package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 191 | FF-SEARCH-COL-dsid: Column search for "DSID" (order). */
public class TC_191_SEARCH_dsid extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 191;
    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 191)
    @Description("TC-191 FF-SEARCH-COL-dsid: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-191_FF-SEARCH-COL-dsid");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
