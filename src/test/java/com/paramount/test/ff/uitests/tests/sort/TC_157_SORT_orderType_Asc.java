package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 157 | FF-SORT-COL-orderType-ASC: Sort "Order Type" ASC (order). */
public class TC_157_SORT_orderType_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 157;
    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 157)
    @Description("TC-157 FF-SORT-COL-orderType-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-157_FF-SORT-COL-orderType-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
