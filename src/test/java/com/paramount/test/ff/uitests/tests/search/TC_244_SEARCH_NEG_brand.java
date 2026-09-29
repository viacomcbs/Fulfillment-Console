package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 244 | FF-SEARCH-COL-NEG-brand: Negative column search for "Brand" (order). */
public class TC_244_SEARCH_NEG_brand extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 244;
    private static final String COLUMN_ID = "brand";
    private static final String COLUMN_NAME = "Brand";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 244)
    @Description("TC-244 FF-SEARCH-COL-NEG-brand: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-244_FF-SEARCH-COL-NEG-brand");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
