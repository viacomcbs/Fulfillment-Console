package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-213 | FF-LI-SEARCH-COL-orderStartDate: Column search for "Order start date" on Line Items tab. */
public class TC_LI_213_SEARCH_orderStartDate extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 213)
    @Description("TC-LI-213 FF-LI-SEARCH-COL-orderStartDate: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-213_FF-LI-SEARCH-COL-orderStartDate");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
