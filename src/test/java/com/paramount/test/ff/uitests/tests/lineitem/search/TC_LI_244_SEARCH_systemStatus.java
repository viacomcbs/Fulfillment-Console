package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-244 | FF-LI-SEARCH-COL-systemStatus: Column search for "System Status" on Line Items tab. */
public class TC_LI_244_SEARCH_systemStatus extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "systemStatus";
    private static final String COLUMN_NAME = "System Status";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 244)
    @Description("TC-LI-244 FF-LI-SEARCH-COL-systemStatus: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-244_FF-LI-SEARCH-COL-systemStatus");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
