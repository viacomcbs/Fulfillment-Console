package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 141 | FF-SORT-COL-orderStartDate-ASC: Sort "Order start date" ASC (order). */
public class TC_141_SORT_orderStartDate_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 141;
    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 141)
    @Description("TC-141 FF-SORT-COL-orderStartDate-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-141_FF-SORT-COL-orderStartDate-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
