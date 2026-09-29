package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 242 | FF-SEARCH-COL-NEG-dsid: Negative column search for "DSID" (order). */
public class TC_242_SEARCH_NEG_dsid extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 242;
    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 242)
    @Description("TC-242 FF-SEARCH-COL-NEG-dsid: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-242_FF-SEARCH-COL-NEG-dsid");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
