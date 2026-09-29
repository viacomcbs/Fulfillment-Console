package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 216 | FF-SEARCH-COL-lineItemId: Column search for "LineItem ID" (lineitem). */
public class TC_216_SEARCH_lineItemId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 216;
    private static final String COLUMN_ID = "lineItemId";
    private static final String COLUMN_NAME = "LineItem ID";
    private static final String SECTION = "lineitem";
    private static final boolean SEARCHABLE = false;
    private static final String SEARCH_TYPE = "NONE";

    @Test(priority = 216)
    @Description("TC-216 FF-SEARCH-COL-lineItemId: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-216_FF-SEARCH-COL-lineItemId");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
