package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-311 | FF-LI-SORT-COL-orderType-ASC: Sort "Order Type" ascending on Line Items tab. */
public class TC_LI_311_SORT_orderType_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 311)
    @Description("TC-LI-311 FF-LI-SORT-COL-orderType-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-311_FF-LI-SORT-COL-orderType-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
