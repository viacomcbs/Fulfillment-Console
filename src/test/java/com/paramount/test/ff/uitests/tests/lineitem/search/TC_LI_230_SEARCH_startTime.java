package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-230 | FF-LI-SEARCH-COL-startTime: Column search for "Start time" on Line Items tab. */
public class TC_LI_230_SEARCH_startTime extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "startTime";
    private static final String COLUMN_NAME = "Start time";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "DATE";

    @Test(priority = 230)
    @Description("TC-LI-230 FF-LI-SEARCH-COL-startTime: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-230_FF-LI-SEARCH-COL-startTime");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
