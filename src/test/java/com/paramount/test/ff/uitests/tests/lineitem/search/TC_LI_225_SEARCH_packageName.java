package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-225 | FF-LI-SEARCH-COL-packageName: Column search for "Package name" on Line Items tab. */
public class TC_LI_225_SEARCH_packageName extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "packageName";
    private static final String COLUMN_NAME = "Package name";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 225)
    @Description("TC-LI-225 FF-LI-SEARCH-COL-packageName: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-225_FF-LI-SEARCH-COL-packageName");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
