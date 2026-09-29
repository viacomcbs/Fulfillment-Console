package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-228 | FF-LI-SEARCH-COL-language: Column search for "Language" on Line Items tab. */
public class TC_LI_228_SEARCH_language extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 228)
    @Description("TC-LI-228 FF-LI-SEARCH-COL-language: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-228_FF-LI-SEARCH-COL-language");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
