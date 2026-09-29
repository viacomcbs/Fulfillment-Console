package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-223 | FF-LI-SEARCH-COL-purchaseOrderId: Column search for "Purchase order ID" on Line Items tab. */
public class TC_LI_223_SEARCH_purchaseOrderId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 223)
    @Description("TC-LI-223 FF-LI-SEARCH-COL-purchaseOrderId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-223_FF-LI-SEARCH-COL-purchaseOrderId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
