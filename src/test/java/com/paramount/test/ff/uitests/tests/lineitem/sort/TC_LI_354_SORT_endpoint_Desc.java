package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-354 | FF-LI-SORT-COL-endpoint-DESC: Sort "Endpoint" descending on Line Items tab. */
public class TC_LI_354_SORT_endpoint_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "endpoint";
    private static final String COLUMN_NAME = "Endpoint";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 354)
    @Description("TC-LI-354 FF-LI-SORT-COL-endpoint-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-354_FF-LI-SORT-COL-endpoint-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
