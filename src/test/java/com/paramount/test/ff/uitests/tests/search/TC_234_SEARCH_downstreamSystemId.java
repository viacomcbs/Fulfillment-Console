package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 234 | FF-SEARCH-COL-downstreamSystemId: Column search for "Downstream System ID" (lineitem). */
public class TC_234_SEARCH_downstreamSystemId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 234;
    private static final String COLUMN_ID = "downstreamSystemId";
    private static final String COLUMN_NAME = "Downstream System ID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 234)
    @Description("TC-234 FF-SEARCH-COL-downstreamSystemId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-234_FF-SEARCH-COL-downstreamSystemId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
