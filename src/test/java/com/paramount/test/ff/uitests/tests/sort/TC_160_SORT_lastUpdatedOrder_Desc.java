package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 160 | FF-SORT-COL-lastUpdatedOrder-DESC: Sort "Last updated" DESC (order). */
public class TC_160_SORT_lastUpdatedOrder_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 160;
    private static final String COLUMN_ID = "lastUpdatedOrder";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 160)
    @Description("TC-160 FF-SORT-COL-lastUpdatedOrder-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-160_FF-SORT-COL-lastUpdatedOrder-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
