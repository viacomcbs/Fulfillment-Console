package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 243 | FF-SEARCH-COL-NEG-partner: Negative column search for "Partner" (order). */
public class TC_243_SEARCH_NEG_partner extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 243;
    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 243)
    @Description("TC-243 FF-SEARCH-COL-NEG-partner: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-243_FF-SEARCH-COL-NEG-partner");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
