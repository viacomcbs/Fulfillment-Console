package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 193 | FF-SEARCH-COL-orderEndDate: Column search for "Order end date" (order). */
public class TC_193_SEARCH_orderEndDate extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 193;
    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 193)
    @Description("TC-193 FF-SEARCH-COL-orderEndDate: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-193_FF-SEARCH-COL-orderEndDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
