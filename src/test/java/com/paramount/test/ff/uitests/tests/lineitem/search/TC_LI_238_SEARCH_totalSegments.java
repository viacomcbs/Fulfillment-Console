package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-238 | FF-LI-SEARCH-COL-totalSegments: Column search for "Total segments" on Line Items tab. */
public class TC_LI_238_SEARCH_totalSegments extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "totalSegments";
    private static final String COLUMN_NAME = "Total segments";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 238)
    @Description("TC-LI-238 FF-LI-SEARCH-COL-totalSegments: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-238_FF-LI-SEARCH-COL-totalSegments");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
