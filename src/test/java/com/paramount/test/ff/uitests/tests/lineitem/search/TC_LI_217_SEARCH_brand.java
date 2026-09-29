package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-217 | FF-LI-SEARCH-COL-brand: Column search for "Brand" on Line Items tab. */
public class TC_LI_217_SEARCH_brand extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 217)
    @Description("TC-LI-217 FF-LI-SEARCH-COL-brand: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-217_FF-LI-SEARCH-COL-brand");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
