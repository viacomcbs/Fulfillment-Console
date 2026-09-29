package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-245 | FF-LI-SEARCH-COL-deliveryOffset: Column search for "Delivery Offset" on Line Items tab. */
public class TC_LI_245_SEARCH_deliveryOffset extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Delivery Offset";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 245)
    @Description("TC-LI-245 FF-LI-SEARCH-COL-deliveryOffset: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-245_FF-LI-SEARCH-COL-deliveryOffset");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
