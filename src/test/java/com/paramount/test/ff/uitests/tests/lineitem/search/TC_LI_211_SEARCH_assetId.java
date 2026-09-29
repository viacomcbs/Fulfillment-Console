package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-211 | FF-LI-SEARCH-COL-assetId: Column search for "Asset ID" on Line Items tab. */
public class TC_LI_211_SEARCH_assetId extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 211)
    @Description("TC-LI-211 FF-LI-SEARCH-COL-assetId: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-211_FF-LI-SEARCH-COL-assetId");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
