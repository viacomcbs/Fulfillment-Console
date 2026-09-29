package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-229 | FF-LI-SEARCH-COL-lastUpdated: Column search for "Last updated" on Line Items tab. */
public class TC_LI_229_SEARCH_lastUpdated extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "lastUpdated";
    private static final String COLUMN_NAME = "Last updated";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 229)
    @Description("TC-LI-229 FF-LI-SEARCH-COL-lastUpdated: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-229_FF-LI-SEARCH-COL-lastUpdated");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
