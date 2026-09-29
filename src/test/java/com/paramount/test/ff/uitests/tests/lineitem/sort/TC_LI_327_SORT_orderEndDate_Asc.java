package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-327 | FF-LI-SORT-COL-orderEndDate-ASC: Sort "Order end date" ascending on Line Items tab. */
public class TC_LI_327_SORT_orderEndDate_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String VALUE_TYPE = "DATE";

    @Test(priority = 327)
    @Description("TC-LI-327 FF-LI-SORT-COL-orderEndDate-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-327_FF-LI-SORT-COL-orderEndDate-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
