package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-212 | FF-LI-SEARCH-COL-dsid: Column search for "DSID" on Line Items tab. */
public class TC_LI_212_SEARCH_dsid extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 212)
    @Description("TC-LI-212 FF-LI-SEARCH-COL-dsid: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-212_FF-LI-SEARCH-COL-dsid");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
