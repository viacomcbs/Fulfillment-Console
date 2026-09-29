package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-227 | FF-LI-SEARCH-COL-endpoint: Column search for "Endpoint" on Line Items tab. */
public class TC_LI_227_SEARCH_endpoint extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "endpoint";
    private static final String COLUMN_NAME = "Endpoint";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 227)
    @Description("TC-LI-227 FF-LI-SEARCH-COL-endpoint: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-227_FF-LI-SEARCH-COL-endpoint");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
