package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 207 | FF-SEARCH-COL-purchaseOrderId: Column search for "Purchase order ID" (order). */
public class TC_207_SEARCH_purchaseOrderId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 207;
    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 207)
    @Description("TC-207 FF-SEARCH-COL-purchaseOrderId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-207_FF-SEARCH-COL-purchaseOrderId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
