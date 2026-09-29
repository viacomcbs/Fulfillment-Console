package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 133 | FF-SORT-COL-orderId-ASC: Sort "Order ID" ASC (order). */
public class TC_133_SORT_orderId_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 133;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 133)
    @Description("TC-133 FF-SORT-COL-orderId-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-133_FF-SORT-COL-orderId-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
