package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-218 | FF-LI-SEARCH-COL-submittedBy: Column search for "Submitted by" on Line Items tab. */
public class TC_LI_218_SEARCH_submittedBy extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 218)
    @Description("TC-LI-218 FF-LI-SEARCH-COL-submittedBy: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-218_FF-LI-SEARCH-COL-submittedBy");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
