package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-318 | FF-LI-SORT-COL-orderIdOrder-DESC: Sort "Order ID (Order level)" descending on Line Items tab. */
public class TC_LI_318_SORT_orderIdOrder_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderIdOrder";
    private static final String COLUMN_NAME = "Order ID (Order level)";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 318)
    @Description("TC-LI-318 FF-LI-SORT-COL-orderIdOrder-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-318_FF-LI-SORT-COL-orderIdOrder-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
