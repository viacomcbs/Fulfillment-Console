package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 181 | FF-SORT-COL-deliveryOffset-ASC: Sort "Delivery Offset" ASC (order). */
public class TC_181_SORT_deliveryOffset_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 181;
    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Delivery Offset";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 181)
    @Description("TC-181 FF-SORT-COL-deliveryOffset-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-181_FF-SORT-COL-deliveryOffset-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
