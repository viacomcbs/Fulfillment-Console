package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 206 | FF-SEARCH-COL-errorMessages: Column search for "Error messages" (order). */
public class TC_206_SEARCH_errorMessages extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 206;
    private static final String COLUMN_ID = "errorMessages";
    private static final String COLUMN_NAME = "Error messages";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 206)
    @Description("TC-206 FF-SEARCH-COL-errorMessages: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-206_FF-SEARCH-COL-errorMessages");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
