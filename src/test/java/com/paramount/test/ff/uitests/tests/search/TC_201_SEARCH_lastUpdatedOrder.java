package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 201 | FF-SEARCH-COL-lastUpdatedOrder: Column search for "Last updated" (order). */
public class TC_201_SEARCH_lastUpdatedOrder extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 201;
    private static final String COLUMN_ID = "lastUpdatedOrder";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 201)
    @Description("TC-201 FF-SEARCH-COL-lastUpdatedOrder: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-201_FF-SEARCH-COL-lastUpdatedOrder");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
