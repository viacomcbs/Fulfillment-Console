package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 138 | FF-SORT-COL-assetId-DESC: Sort "Asset ID" DESC (order). */
public class TC_138_SORT_assetId_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 138;
    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 138)
    @Description("TC-138 FF-SORT-COL-assetId-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-138_FF-SORT-COL-assetId-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
