package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 215 | FF-SEARCH-COL-type: Column search for "Type" (lineitem). */
public class TC_215_SEARCH_type extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 215;
    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 215)
    @Description("TC-215 FF-SEARCH-COL-type: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-215_FF-SEARCH-COL-type");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
