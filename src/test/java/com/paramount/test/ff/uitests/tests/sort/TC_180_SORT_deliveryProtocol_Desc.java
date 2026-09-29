package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 180 | FF-SORT-COL-deliveryProtocol-DESC: Sort "Delivery Protocol" DESC (order). */
public class TC_180_SORT_deliveryProtocol_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 180;
    private static final String COLUMN_ID = "deliveryProtocol";
    private static final String COLUMN_NAME = "Delivery Protocol";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 180)
    @Description("TC-180 FF-SORT-COL-deliveryProtocol-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-180_FF-SORT-COL-deliveryProtocol-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
