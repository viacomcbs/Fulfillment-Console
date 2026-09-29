package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-336 | FF-LI-SORT-COL-submittedBy-DESC: Sort "Submitted by" descending on Line Items tab. */
public class TC_LI_336_SORT_submittedBy_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 336)
    @Description("TC-LI-336 FF-LI-SORT-COL-submittedBy-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-336_FF-LI-SORT-COL-submittedBy-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
