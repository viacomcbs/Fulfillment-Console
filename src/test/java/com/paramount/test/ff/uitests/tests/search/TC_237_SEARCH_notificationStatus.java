package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 237 | FF-SEARCH-COL-notificationStatus: Column search for "Notification Status" (lineitem). */
public class TC_237_SEARCH_notificationStatus extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 237;
    private static final String COLUMN_ID = "notificationStatus";
    private static final String COLUMN_NAME = "Notification Status";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 237)
    @Description("TC-237 FF-SEARCH-COL-notificationStatus: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-237_FF-SEARCH-COL-notificationStatus");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
