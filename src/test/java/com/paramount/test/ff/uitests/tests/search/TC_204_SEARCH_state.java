package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 204 | FF-SEARCH-COL-state: Column search for "State" (order). */
public class TC_204_SEARCH_state extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 204;
    private static final String COLUMN_ID = "state";
    private static final String COLUMN_NAME = "State";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 204)
    @Description("TC-204 FF-SEARCH-COL-state: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-204_FF-SEARCH-COL-state");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
