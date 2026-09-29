package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-214 | FF-LI-SEARCH-COL-orderEndDate: Column search for "Order end date" on Line Items tab. */
public class TC_LI_214_SEARCH_orderEndDate extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 214)
    @Description("TC-LI-214 FF-LI-SEARCH-COL-orderEndDate: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-214_FF-LI-SEARCH-COL-orderEndDate");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
