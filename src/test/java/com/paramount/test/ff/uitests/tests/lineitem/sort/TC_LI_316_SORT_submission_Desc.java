package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-316 | FF-LI-SORT-COL-submission-DESC: Sort "Submission" descending on Line Items tab. */
public class TC_LI_316_SORT_submission_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 316)
    @Description("TC-LI-316 FF-LI-SORT-COL-submission-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-316_FF-LI-SORT-COL-submission-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
