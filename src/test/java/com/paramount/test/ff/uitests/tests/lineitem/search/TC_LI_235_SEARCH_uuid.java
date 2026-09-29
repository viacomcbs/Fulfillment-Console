package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-235 | FF-LI-SEARCH-COL-uuid: Column search for "UUID" on Line Items tab. */
public class TC_LI_235_SEARCH_uuid extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "uuid";
    private static final String COLUMN_NAME = "UUID";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 235)
    @Description("TC-LI-235 FF-LI-SEARCH-COL-uuid: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-235_FF-LI-SEARCH-COL-uuid");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
