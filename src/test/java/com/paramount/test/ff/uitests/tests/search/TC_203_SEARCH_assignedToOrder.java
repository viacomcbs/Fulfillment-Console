package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 203 | FF-SEARCH-COL-assignedToOrder: Column search for "Assigned to" (order). */
public class TC_203_SEARCH_assignedToOrder extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 203;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 203)
    @Description("TC-203 FF-SEARCH-COL-assignedToOrder: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-203_FF-SEARCH-COL-assignedToOrder");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
