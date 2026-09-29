package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-346 | FF-LI-SORT-COL-purchaseOrderId-DESC: Sort "Purchase order ID" descending on Line Items tab. */
public class TC_LI_346_SORT_purchaseOrderId_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 346)
    @Description("TC-LI-346 FF-LI-SORT-COL-purchaseOrderId-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-346_FF-LI-SORT-COL-purchaseOrderId-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
