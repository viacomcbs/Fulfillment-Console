package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 143 | FF-SORT-COL-orderEndDate-ASC: Sort "Order end date" ASC (order). */
public class TC_143_SORT_orderEndDate_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 143;
    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 143)
    @Description("TC-143 FF-SORT-COL-orderEndDate-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-143_FF-SORT-COL-orderEndDate-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
