package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 188 | FF-SEARCH-COL-orderId: Column search for "Order ID" (order). */
public class TC_188_SEARCH_orderId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 188;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 188)
    @Description("TC-188 FF-SEARCH-COL-orderId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-188_FF-SEARCH-COL-orderId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
