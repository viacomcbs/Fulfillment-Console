package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 195 | FF-SEARCH-COL-brand: Column search for "Brand" (order). */
public class TC_195_SEARCH_brand extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 195;
    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String SECTION = "order";
    private static final boolean SEARCHABLE = true;
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 195)
    @Description("TC-195 FF-SEARCH-COL-brand: Verify column search/filter on Orders grid")
    public void validateColumnSearchFunctionality() throws InterruptedException {
        initSoftAssert("TC-195_FF-SEARCH-COL-brand");
        runColumnSearchTest(COLUMN_NAME, SECTION, SEARCHABLE, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
