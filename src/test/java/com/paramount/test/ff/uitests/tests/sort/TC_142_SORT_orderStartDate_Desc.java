package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 142 | FF-SORT-COL-orderStartDate-DESC: Sort "Order start date" DESC (order). */
public class TC_142_SORT_orderStartDate_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 142;
    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 142)
    @Description("TC-142 FF-SORT-COL-orderStartDate-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-142_FF-SORT-COL-orderStartDate-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
