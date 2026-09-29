package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-209 | FF-LI-SEARCH-COL-orderIdOrder: Column search for "Order ID (Order level)" on Line Items tab. */
public class TC_LI_209_SEARCH_orderIdOrder extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "orderIdOrder";
    private static final String COLUMN_NAME = "Order ID (Order level)";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 209)
    @Description("TC-LI-209 FF-LI-SEARCH-COL-orderIdOrder: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-209_FF-LI-SEARCH-COL-orderIdOrder");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
