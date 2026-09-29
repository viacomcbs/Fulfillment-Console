package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 171 | FF-SORT-COL-purchaseOrderId-ASC: Sort "Purchase order ID" ASC (order). */
public class TC_171_SORT_purchaseOrderId_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 171;
    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 171)
    @Description("TC-171 FF-SORT-COL-purchaseOrderId-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-171_FF-SORT-COL-purchaseOrderId-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
