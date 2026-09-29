package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-224 | FF-LI-SEARCH-COL-fastTrack: Column search for "Fast Track" on Line Items tab. */
public class TC_LI_224_SEARCH_fastTrack extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 224)
    @Description("TC-LI-224 FF-LI-SEARCH-COL-fastTrack: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-224_FF-LI-SEARCH-COL-fastTrack");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
