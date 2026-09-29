package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 159 | FF-SORT-COL-lastUpdatedOrder-ASC: Sort "Last updated" ASC (order). */
public class TC_159_SORT_lastUpdatedOrder_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 159;
    private static final String COLUMN_ID = "lastUpdatedOrder";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 159)
    @Description("TC-159 FF-SORT-COL-lastUpdatedOrder-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-159_FF-SORT-COL-lastUpdatedOrder-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
