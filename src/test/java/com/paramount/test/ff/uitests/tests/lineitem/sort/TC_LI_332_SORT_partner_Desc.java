package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-332 | FF-LI-SORT-COL-partner-DESC: Sort "Partner" descending on Line Items tab. */
public class TC_LI_332_SORT_partner_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 332)
    @Description("TC-LI-332 FF-LI-SORT-COL-partner-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-332_FF-LI-SORT-COL-partner-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
