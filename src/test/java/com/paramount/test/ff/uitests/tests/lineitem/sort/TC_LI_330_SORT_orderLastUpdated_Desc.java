package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-330 | FF-LI-SORT-COL-orderLastUpdated-DESC: Sort "Order last updated" descending on Line Items tab. */
public class TC_LI_330_SORT_orderLastUpdated_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderLastUpdated";
    private static final String COLUMN_NAME = "Order last updated";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 330)
    @Description("TC-LI-330 FF-LI-SORT-COL-orderLastUpdated-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-330_FF-LI-SORT-COL-orderLastUpdated-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
