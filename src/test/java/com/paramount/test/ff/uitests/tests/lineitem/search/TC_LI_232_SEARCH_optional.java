package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-232 | FF-LI-SEARCH-COL-optional: Column search for "Optional" on Line Items tab. */
public class TC_LI_232_SEARCH_optional extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "optional";
    private static final String COLUMN_NAME = "Optional";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 232)
    @Description("TC-LI-232 FF-LI-SEARCH-COL-optional: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-232_FF-LI-SEARCH-COL-optional");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
