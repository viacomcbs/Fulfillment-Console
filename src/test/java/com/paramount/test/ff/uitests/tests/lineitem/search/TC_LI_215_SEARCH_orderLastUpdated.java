package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-215 | FF-LI-SEARCH-COL-orderLastUpdated: Column search for "Order last updated" on Line Items tab. */
public class TC_LI_215_SEARCH_orderLastUpdated extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "orderLastUpdated";
    private static final String COLUMN_NAME = "Order last updated";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 215)
    @Description("TC-LI-215 FF-LI-SEARCH-COL-orderLastUpdated: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-215_FF-LI-SEARCH-COL-orderLastUpdated");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
