package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 220 | FF-SEARCH-COL-language: Column search for "Language" (lineitem). */
public class TC_220_SEARCH_language extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 220;
    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 220)
    @Description("TC-220 FF-SEARCH-COL-language: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-220_FF-SEARCH-COL-language");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
