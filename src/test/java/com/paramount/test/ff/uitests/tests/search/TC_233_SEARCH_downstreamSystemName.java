package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 233 | FF-SEARCH-COL-downstreamSystemName: Column search for "Downstream System Name" (lineitem). */
public class TC_233_SEARCH_downstreamSystemName extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 233;
    private static final String COLUMN_ID = "downstreamSystemName";
    private static final String COLUMN_NAME = "Downstream System Name";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 233)
    @Description("TC-233 FF-SEARCH-COL-downstreamSystemName: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-233_FF-SEARCH-COL-downstreamSystemName");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
