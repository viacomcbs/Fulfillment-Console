package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-233 | FF-LI-SEARCH-COL-assignedTo: Column search for "Assigned to" on Line Items tab. */
public class TC_LI_233_SEARCH_assignedTo extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "assignedTo";
    private static final String COLUMN_NAME = "Assigned to";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 233)
    @Description("TC-LI-233 FF-LI-SEARCH-COL-assignedTo: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-233_FF-LI-SEARCH-COL-assignedTo");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
