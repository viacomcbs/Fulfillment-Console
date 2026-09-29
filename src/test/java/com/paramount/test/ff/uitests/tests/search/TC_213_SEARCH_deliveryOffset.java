package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 213 | FF-SEARCH-COL-deliveryOffset: Column search for "Offset Delivery date" (order). */
public class TC_213_SEARCH_deliveryOffset extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 213;
    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Offset Delivery date";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 213)
    @Description("TC-213 FF-SEARCH-COL-deliveryOffset: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-213_FF-SEARCH-COL-deliveryOffset");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
