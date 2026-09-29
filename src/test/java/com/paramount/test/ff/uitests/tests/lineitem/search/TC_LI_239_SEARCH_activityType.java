package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-239 | FF-LI-SEARCH-COL-activityType: Column search for "Activity Type" on Line Items tab. */
public class TC_LI_239_SEARCH_activityType extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "activityType";
    private static final String COLUMN_NAME = "Activity Type";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 239)
    @Description("TC-LI-239 FF-LI-SEARCH-COL-activityType: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-239_FF-LI-SEARCH-COL-activityType");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
