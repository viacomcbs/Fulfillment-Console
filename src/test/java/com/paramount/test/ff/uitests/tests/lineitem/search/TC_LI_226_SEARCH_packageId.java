package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-226 | FF-LI-SEARCH-COL-packageId: Column search for "Package ID" on Line Items tab. */
public class TC_LI_226_SEARCH_packageId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "packageId";
    private static final String COLUMN_NAME = "Package ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 226)
    @Description("TC-LI-226 FF-LI-SEARCH-COL-packageId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-226_FF-LI-SEARCH-COL-packageId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
