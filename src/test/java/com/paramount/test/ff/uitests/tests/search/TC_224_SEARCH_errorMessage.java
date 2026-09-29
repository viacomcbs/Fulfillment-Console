package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 224 | FF-SEARCH-COL-errorMessage: Column search for "Error message" (lineitem). */
public class TC_224_SEARCH_errorMessage extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 224;
    private static final String COLUMN_ID = "errorMessage";
    private static final String COLUMN_NAME = "Error message";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 224)
    @Description("TC-224 FF-SEARCH-COL-errorMessage: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-224_FF-SEARCH-COL-errorMessage");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
