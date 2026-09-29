package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-312 | FF-LI-SORT-COL-orderType-DESC: Sort "Order Type" descending on Line Items tab. */
public class TC_LI_312_SORT_orderType_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 312)
    @Description("TC-LI-312 FF-LI-SORT-COL-orderType-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-312_FF-LI-SORT-COL-orderType-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
