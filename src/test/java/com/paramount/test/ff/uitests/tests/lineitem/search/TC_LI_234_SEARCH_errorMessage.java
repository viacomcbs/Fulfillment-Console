package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-234 | FF-LI-SEARCH-COL-errorMessage: Column search for "Error message" on Line Items tab. */
public class TC_LI_234_SEARCH_errorMessage extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "errorMessage";
    private static final String COLUMN_NAME = "Error message";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 234)
    @Description("TC-LI-234 FF-LI-SEARCH-COL-errorMessage: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-234_FF-LI-SEARCH-COL-errorMessage");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
