package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-220 | FF-LI-SEARCH-COL-contentType: Column search for "Content type" on Line Items tab. */
public class TC_LI_220_SEARCH_contentType extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "contentType";
    private static final String COLUMN_NAME = "Content type";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 220)
    @Description("TC-LI-220 FF-LI-SEARCH-COL-contentType: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-220_FF-LI-SEARCH-COL-contentType");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
