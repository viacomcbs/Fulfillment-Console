package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 221 | FF-SEARCH-COL-lastUpdatedLineItem: Column search for "Last updated" (lineitem). */
public class TC_221_SEARCH_lastUpdatedLineItem extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 221;
    private static final String COLUMN_ID = "lastUpdatedLineItem";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 221)
    @Description("TC-221 FF-SEARCH-COL-lastUpdatedLineItem: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-221_FF-SEARCH-COL-lastUpdatedLineItem");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
