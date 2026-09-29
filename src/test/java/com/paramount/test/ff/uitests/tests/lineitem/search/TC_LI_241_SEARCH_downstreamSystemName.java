package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-241 | FF-LI-SEARCH-COL-downstreamSystemName: Column search for "Downstream System Name" on Line Items tab. */
public class TC_LI_241_SEARCH_downstreamSystemName extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "downstreamSystemName";
    private static final String COLUMN_NAME = "Downstream System Name";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 241)
    @Description("TC-LI-241 FF-LI-SEARCH-COL-downstreamSystemName: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-241_FF-LI-SEARCH-COL-downstreamSystemName");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
