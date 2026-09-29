package com.paramount.test.ff.uitests.tests.lineitem.search;

import com.paramount.test.ff.uitests.base.LineItemTabSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-246 | FF-LI-SEARCH-COL-notificationStatus: Column search for "Notification Status" on Line Items tab. */
public class TC_LI_246_SEARCH_notificationStatus extends LineItemTabSearchBaseTest {

    private static final String COLUMN_ID = "notificationStatus";
    private static final String COLUMN_NAME = "Notification Status";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 246)
    @Description("TC-LI-246 FF-LI-SEARCH-COL-notificationStatus: Verify column search/filter on Line Items tab")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-LI-246_FF-LI-SEARCH-COL-notificationStatus");
        runLineItemColumnSearchTest(COLUMN_NAME, COLUMN_ID, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
