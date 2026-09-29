package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-208 | FF-LI-SEARCH-COL-submission: Column search for "Submission" on Line Items tab. */
public class TC_LI_208_SEARCH_submission extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 208)
    @Description("TC-LI-208 FF-LI-SEARCH-COL-submission: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-208_FF-LI-SEARCH-COL-submission");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
