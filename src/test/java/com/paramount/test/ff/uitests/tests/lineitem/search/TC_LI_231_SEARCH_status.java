package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-231 | FF-LI-SEARCH-COL-status: Column search for "Status" on Line Items tab. */
public class TC_LI_231_SEARCH_status extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "status";
    private static final String COLUMN_NAME = "Status";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 231)
    @Description("TC-LI-231 FF-LI-SEARCH-COL-status: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-231_FF-LI-SEARCH-COL-status");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
