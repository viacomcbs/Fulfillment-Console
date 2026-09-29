package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 199 | FF-SEARCH-COL-contentTypeOrder: Column search for "Content type" (order). */
public class TC_199_SEARCH_contentTypeOrder extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 199;
    private static final String COLUMN_ID = "contentTypeOrder";
    private static final String COLUMN_NAME = "Content type";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 199)
    @Description("TC-199 FF-SEARCH-COL-contentTypeOrder: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-199_FF-SEARCH-COL-contentTypeOrder");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
