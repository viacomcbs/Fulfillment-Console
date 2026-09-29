package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-222 | FF-LI-SEARCH-COL-priority: Column search for "Priority" on Line Items tab. */
public class TC_LI_222_SEARCH_priority extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 222)
    @Description("TC-LI-222 FF-LI-SEARCH-COL-priority: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-222_FF-LI-SEARCH-COL-priority");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
