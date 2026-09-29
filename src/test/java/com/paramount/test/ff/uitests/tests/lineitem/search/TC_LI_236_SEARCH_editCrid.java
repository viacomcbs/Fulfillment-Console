package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-236 | FF-LI-SEARCH-COL-editCrid: Column search for "Edit CRID" on Line Items tab. */
public class TC_LI_236_SEARCH_editCrid extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "editCrid";
    private static final String COLUMN_NAME = "Edit CRID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 236)
    @Description("TC-LI-236 FF-LI-SEARCH-COL-editCrid: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-236_FF-LI-SEARCH-COL-editCrid");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
