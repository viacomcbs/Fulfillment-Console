package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 197 | FF-SEARCH-COL-submittedBy: Column search for "Submitted by" (order). */
public class TC_197_SEARCH_submittedBy extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 197;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 197)
    @Description("TC-197 FF-SEARCH-COL-submittedBy: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-197_FF-SEARCH-COL-submittedBy");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
