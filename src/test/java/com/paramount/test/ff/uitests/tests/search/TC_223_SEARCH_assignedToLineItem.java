package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 223 | FF-SEARCH-COL-assignedToLineItem: Column search for "Assigned to" (lineitem). */
public class TC_223_SEARCH_assignedToLineItem extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 223;
    private static final String COLUMN_ID = "assignedToLineItem";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 223)
    @Description("TC-223 FF-SEARCH-COL-assignedToLineItem: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-223_FF-SEARCH-COL-assignedToLineItem");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
