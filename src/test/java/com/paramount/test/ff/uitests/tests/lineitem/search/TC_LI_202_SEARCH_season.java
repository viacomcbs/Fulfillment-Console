package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-202 | FF-LI-SEARCH-COL-season: Column search for "Season" on Line Items tab. */
public class TC_LI_202_SEARCH_season extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 202)
    @Description("TC-LI-202 FF-LI-SEARCH-COL-season: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-202_FF-LI-SEARCH-COL-season");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
