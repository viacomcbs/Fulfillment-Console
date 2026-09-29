package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-240 | FF-LI-SEARCH-COL-cbsId: Column search for "CBS ID" on Line Items tab. */
public class TC_LI_240_SEARCH_cbsId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "cbsId";
    private static final String COLUMN_NAME = "CBS ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 240)
    @Description("TC-LI-240 FF-LI-SEARCH-COL-cbsId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-240_FF-LI-SEARCH-COL-cbsId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
