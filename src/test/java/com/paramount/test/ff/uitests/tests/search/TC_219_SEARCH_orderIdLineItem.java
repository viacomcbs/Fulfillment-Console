package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 219 | FF-SEARCH-COL-orderIdLineItem: Column search for "Order ID" (lineitem). */
public class TC_219_SEARCH_orderIdLineItem extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 219;
    private static final String COLUMN_ID = "orderIdLineItem";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 219)
    @Description("TC-219 FF-SEARCH-COL-orderIdLineItem: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-219_FF-SEARCH-COL-orderIdLineItem");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
