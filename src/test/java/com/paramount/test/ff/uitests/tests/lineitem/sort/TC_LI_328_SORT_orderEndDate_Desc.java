package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-328 | FF-LI-SORT-COL-orderEndDate-DESC: Sort "Order end date" descending on Line Items tab. */
public class TC_LI_328_SORT_orderEndDate_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 328)
    @Description("TC-LI-328 FF-LI-SORT-COL-orderEndDate-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-328_FF-LI-SORT-COL-orderEndDate-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
