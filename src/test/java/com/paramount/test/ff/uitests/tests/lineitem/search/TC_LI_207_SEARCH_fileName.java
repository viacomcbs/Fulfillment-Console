package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-207 | FF-LI-SEARCH-COL-fileName: Column search for "File name" on Line Items tab. */
public class TC_LI_207_SEARCH_fileName extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 207)
    @Description("TC-LI-207 FF-LI-SEARCH-COL-fileName: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-207_FF-LI-SEARCH-COL-fileName");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
