package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 239 | FF-SEARCH-COL-NEG-orderId: Negative column search for "Order ID" (order). */
public class TC_239_SEARCH_NEG_orderId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 239;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 239)
    @Description("TC-239 FF-SEARCH-COL-NEG-orderId: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-239_FF-SEARCH-COL-NEG-orderId");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
