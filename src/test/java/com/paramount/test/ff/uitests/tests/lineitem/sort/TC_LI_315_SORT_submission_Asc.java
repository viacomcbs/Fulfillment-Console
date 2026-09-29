package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-315 | FF-LI-SORT-COL-submission-ASC: Sort "Submission" ascending on Line Items tab. */
public class TC_LI_315_SORT_submission_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 315)
    @Description("TC-LI-315 FF-LI-SORT-COL-submission-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-315_FF-LI-SORT-COL-submission-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
