package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 222 | FF-SEARCH-COL-optional: Column search for "Optional" (lineitem). */
public class TC_222_SEARCH_optional extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 222;
    private static final String COLUMN_ID = "optional";
    private static final String COLUMN_NAME = "Optional";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 222)
    @Description("TC-222 FF-SEARCH-COL-optional: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-222_FF-SEARCH-COL-optional");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
