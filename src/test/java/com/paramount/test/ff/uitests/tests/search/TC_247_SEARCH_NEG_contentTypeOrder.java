package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 247 | FF-SEARCH-COL-NEG-contentTypeOrder: Negative column search for "Content type" (order). */
public class TC_247_SEARCH_NEG_contentTypeOrder extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 247;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 247)
    @Description("TC-247 FF-SEARCH-COL-NEG-contentTypeOrder: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-247_FF-SEARCH-COL-NEG-contentTypeOrder");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
