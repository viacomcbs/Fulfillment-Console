package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-345 | FF-LI-SORT-COL-purchaseOrderId-ASC: Sort "Purchase order ID" ascending on Line Items tab. */
public class TC_LI_345_SORT_purchaseOrderId_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 345)
    @Description("TC-LI-345 FF-LI-SORT-COL-purchaseOrderId-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-345_FF-LI-SORT-COL-purchaseOrderId-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
