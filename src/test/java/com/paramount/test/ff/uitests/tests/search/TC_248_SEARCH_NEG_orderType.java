package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 248 | FF-SEARCH-COL-NEG-orderType: Negative column search for "Order Type" (order). */
public class TC_248_SEARCH_NEG_orderType extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 248;
    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 248)
    @Description("TC-248 FF-SEARCH-COL-NEG-orderType: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-248_FF-SEARCH-COL-NEG-orderType");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
