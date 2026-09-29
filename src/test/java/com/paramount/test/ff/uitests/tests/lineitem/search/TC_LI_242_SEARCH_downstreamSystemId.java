package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-242 | FF-LI-SEARCH-COL-downstreamSystemId: Column search for "Downstream System ID" on Line Items tab. */
public class TC_LI_242_SEARCH_downstreamSystemId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "downstreamSystemId";
    private static final String COLUMN_NAME = "Downstream System ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 242)
    @Description("TC-LI-242 FF-LI-SEARCH-COL-downstreamSystemId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-242_FF-LI-SEARCH-COL-downstreamSystemId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
