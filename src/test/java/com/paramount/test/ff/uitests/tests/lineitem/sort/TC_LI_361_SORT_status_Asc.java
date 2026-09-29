package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-361 | FF-LI-SORT-COL-status-ASC: Sort "Status" ascending on Line Items tab. */
public class TC_LI_361_SORT_status_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "status";
    private static final String COLUMN_NAME = "Status";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 361)
    @Description("TC-LI-361 FF-LI-SORT-COL-status-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-361_FF-LI-SORT-COL-status-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
