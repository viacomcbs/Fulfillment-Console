package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 137 | FF-SORT-COL-assetId-ASC: Sort "Asset ID" ASC (order). */
public class TC_137_SORT_assetId_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 137;
    private static final String COLUMN_ID = "assetId";
    private static final String COLUMN_NAME = "Asset ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 137)
    @Description("TC-137 FF-SORT-COL-assetId-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-137_FF-SORT-COL-assetId-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
