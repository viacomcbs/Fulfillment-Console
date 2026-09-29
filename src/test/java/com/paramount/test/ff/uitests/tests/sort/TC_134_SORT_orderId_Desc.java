package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 134 | FF-SORT-COL-orderId-DESC: Sort "Order ID" DESC (order). */
public class TC_134_SORT_orderId_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 134;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 134)
    @Description("TC-134 FF-SORT-COL-orderId-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-134_FF-SORT-COL-orderId-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
