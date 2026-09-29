package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 192 | FF-SEARCH-COL-orderStartDate: Column search for "Order start date" (order). */
public class TC_192_SEARCH_orderStartDate extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 192;
    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 192)
    @Description("TC-192 FF-SEARCH-COL-orderStartDate: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-192_FF-SEARCH-COL-orderStartDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
