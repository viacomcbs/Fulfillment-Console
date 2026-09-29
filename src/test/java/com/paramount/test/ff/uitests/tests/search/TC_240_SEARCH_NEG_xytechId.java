package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 240 | FF-SEARCH-COL-NEG-xytechId: Negative column search for "Xytech ID" (order). */
public class TC_240_SEARCH_NEG_xytechId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 240;
    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 240)
    @Description("TC-240 FF-SEARCH-COL-NEG-xytechId: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-240_FF-SEARCH-COL-NEG-xytechId");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
