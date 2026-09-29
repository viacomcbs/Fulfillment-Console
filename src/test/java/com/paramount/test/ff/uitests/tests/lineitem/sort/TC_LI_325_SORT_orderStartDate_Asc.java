package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-325 | FF-LI-SORT-COL-orderStartDate-ASC: Sort "Order start date" ascending on Line Items tab. */
public class TC_LI_325_SORT_orderStartDate_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderStartDate";
    private static final String COLUMN_NAME = "Order start date";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 325)
    @Description("TC-LI-325 FF-LI-SORT-COL-orderStartDate-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-325_FF-LI-SORT-COL-orderStartDate-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
