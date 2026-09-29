package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-362 | FF-LI-SORT-COL-status-DESC: Sort "Status" descending on Line Items tab. */
public class TC_LI_362_SORT_status_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "status";
    private static final String COLUMN_NAME = "Status";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 362)
    @Description("TC-LI-362 FF-LI-SORT-COL-status-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-362_FF-LI-SORT-COL-status-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
