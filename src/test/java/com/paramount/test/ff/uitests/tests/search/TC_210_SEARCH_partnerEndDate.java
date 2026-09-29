package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 210 | FF-SEARCH-COL-partnerEndDate: Column search for "Partner end date" (order). */
public class TC_210_SEARCH_partnerEndDate extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 210;
    private static final String COLUMN_ID = "partnerEndDate";
    private static final String COLUMN_NAME = "Partner end date";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 210)
    @Description("TC-210 FF-SEARCH-COL-partnerEndDate: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-210_FF-SEARCH-COL-partnerEndDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
