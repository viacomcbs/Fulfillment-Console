package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-326 | FF-LI-SORT-COL-orderStartDate-DESC: Sort "Order start date" descending on Line Items tab. */
public class TC_LI_326_SORT_orderStartDate_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 326)
    @Description("TC-LI-326 FF-LI-SORT-COL-orderStartDate-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-326_FF-LI-SORT-COL-orderStartDate-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
