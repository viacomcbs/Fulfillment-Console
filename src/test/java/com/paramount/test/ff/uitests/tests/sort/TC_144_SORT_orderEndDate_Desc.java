package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 144 | FF-SORT-COL-orderEndDate-DESC: Sort "Order end date" DESC (order). */
public class TC_144_SORT_orderEndDate_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 144;
    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 144)
    @Description("TC-144 FF-SORT-COL-orderEndDate-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-144_FF-SORT-COL-orderEndDate-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
