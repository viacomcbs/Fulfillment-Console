package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 212 | FF-SEARCH-COL-deliveryDate: Column search for "Delivery date" (order). */
public class TC_212_SEARCH_deliveryDate extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 212;
    private static final String COLUMN_ID = "deliveryDate";
    private static final String COLUMN_NAME = "Delivery date";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 212)
    @Description("TC-212 FF-SEARCH-COL-deliveryDate: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-212_FF-SEARCH-COL-deliveryDate");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
