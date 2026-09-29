package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 226 | FF-SEARCH-COL-uuid: Column search for "UUID" (lineitem). */
public class TC_226_SEARCH_uuid extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 226;
    private static final String COLUMN_ID = "uuid";
    private static final String COLUMN_NAME = "UUID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 226)
    @Description("TC-226 FF-SEARCH-COL-uuid: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-226_FF-SEARCH-COL-uuid");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
