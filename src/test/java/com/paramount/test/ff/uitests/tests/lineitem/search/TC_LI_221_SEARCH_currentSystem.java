package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-221 | FF-LI-SEARCH-COL-currentSystem: Column search for "Current system" on Line Items tab. */
public class TC_LI_221_SEARCH_currentSystem extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 221)
    @Description("TC-LI-221 FF-LI-SEARCH-COL-currentSystem: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-221_FF-LI-SEARCH-COL-currentSystem");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
