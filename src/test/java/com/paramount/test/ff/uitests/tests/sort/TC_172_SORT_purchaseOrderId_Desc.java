package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 172 | FF-SORT-COL-purchaseOrderId-DESC: Sort "Purchase order ID" DESC (order). */
public class TC_172_SORT_purchaseOrderId_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 172;
    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 172)
    @Description("TC-172 FF-SORT-COL-purchaseOrderId-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-172_FF-SORT-COL-purchaseOrderId-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
