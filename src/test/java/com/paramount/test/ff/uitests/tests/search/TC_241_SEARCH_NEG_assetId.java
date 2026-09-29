package com.paramount.test.ff.uitests.tests.search;

import com.paramount.test.ff.uitests.base.ColumnSearchBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 241 | FF-SEARCH-COL-NEG-assetId: Negative column search for "Asset ID" (order). */
public class TC_241_SEARCH_NEG_assetId extends ColumnSearchBaseTest {

    private static final int TC_NUMBER = 241;
    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String SECTION = "order";
    private static final String SEARCH_TYPE = "TEXT";

    @Test(priority = 241)
    @Description("TC-241 FF-SEARCH-COL-NEG-assetId: Verify no-match column search filters Orders grid")
    public void validateNegativeColumnSearch() throws InterruptedException {
        initSoftAssert("TC-241_FF-SEARCH-COL-NEG-assetId");
        runColumnSearchNegativeTest(COLUMN_NAME, SECTION, SEARCH_TYPE);
        softAssert.assertAll();
    }
}
