package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-205 | FF-LI-SEARCH-COL-type: Column search for "Type" on Line Items tab. */
public class TC_LI_205_SEARCH_type extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "type";
    private static final String COLUMN_NAME = "Type";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 205)
    @Description("TC-LI-205 FF-LI-SEARCH-COL-type: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-205_FF-LI-SEARCH-COL-type");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
