package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 182 | FF-SORT-COL-deliveryOffset-DESC: Sort "Delivery Offset" DESC (order). */
public class TC_182_SORT_deliveryOffset_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 182;
    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Delivery Offset";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 182)
    @Description("TC-182 FF-SORT-COL-deliveryOffset-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-182_FF-SORT-COL-deliveryOffset-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
