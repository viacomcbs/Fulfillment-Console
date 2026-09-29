package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 230 | FF-SEARCH-COL-totalSegments: Column search for "Total segments" (lineitem). */
public class TC_230_SEARCH_totalSegments extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 230;
    private static final String COLUMN_ID = "totalSegments";
    private static final String COLUMN_NAME = "Total segments";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 230)
    @Description("TC-230 FF-SEARCH-COL-totalSegments: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-230_FF-SEARCH-COL-totalSegments");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
