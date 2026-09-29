package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-317 | FF-LI-SORT-COL-orderIdOrder-ASC: Sort "Order ID (Order level)" ascending on Line Items tab. */
public class TC_LI_317_SORT_orderIdOrder_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderIdOrder";
    private static final String COLUMN_NAME = "Order ID (Order level)";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 317)
    @Description("TC-LI-317 FF-LI-SORT-COL-orderIdOrder-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-317_FF-LI-SORT-COL-orderIdOrder-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
