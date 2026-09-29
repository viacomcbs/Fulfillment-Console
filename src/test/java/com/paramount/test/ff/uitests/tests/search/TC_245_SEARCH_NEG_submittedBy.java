package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 245 | FF-SEARCH-COL-NEG-submittedBy: Negative column search for "Submitted by" (order). */
public class TC_245_SEARCH_NEG_submittedBy extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 245;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 245)
    @Description("TC-245 FF-SEARCH-COL-NEG-submittedBy: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-245_FF-SEARCH-COL-NEG-submittedBy");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
