package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 232 | FF-SEARCH-COL-cbsId: Column search for "CBS ID" (lineitem). */
public class TC_232_SEARCH_cbsId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 232;
    private static final String COLUMN_ID = "cbsId";
    private static final String COLUMN_NAME = "CBS ID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 232)
    @Description("TC-232 FF-SEARCH-COL-cbsId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-232_FF-SEARCH-COL-cbsId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
