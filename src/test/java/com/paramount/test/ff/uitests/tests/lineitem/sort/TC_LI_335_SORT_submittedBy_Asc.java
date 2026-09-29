package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-335 | FF-LI-SORT-COL-submittedBy-ASC: Sort "Submitted by" ascending on Line Items tab. */
public class TC_LI_335_SORT_submittedBy_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 335)
    @Description("TC-LI-335 FF-LI-SORT-COL-submittedBy-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-335_FF-LI-SORT-COL-submittedBy-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
