package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 205 | FF-SEARCH-COL-priority: Column search for "Priority" (order). */
public class TC_205_SEARCH_priority extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 205;
    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 205)
    @Description("TC-205 FF-SEARCH-COL-priority: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-205_FF-SEARCH-COL-priority");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
