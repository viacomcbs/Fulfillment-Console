package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 236 | FF-SEARCH-COL-systemStatus: Column search for "System Status" (lineitem). */
public class TC_236_SEARCH_systemStatus extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 236;
    private static final String COLUMN_ID = "systemStatus";
    private static final String COLUMN_NAME = "System Status";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 236)
    @Description("TC-236 FF-SEARCH-COL-systemStatus: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-236_FF-SEARCH-COL-systemStatus");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
