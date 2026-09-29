package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-329 | FF-LI-SORT-COL-orderLastUpdated-ASC: Sort "Order last updated" ascending on Line Items tab. */
public class TC_LI_329_SORT_orderLastUpdated_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderLastUpdated";
    private static final String COLUMN_NAME = "Order last updated";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 329)
    @Description("TC-LI-329 FF-LI-SORT-COL-orderLastUpdated-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-329_FF-LI-SORT-COL-orderLastUpdated-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
