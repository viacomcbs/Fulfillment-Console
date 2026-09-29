package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-026 | FF-LI-MC-COL-submission-HIDE: Disable "Submission" on Line Items tab. */
public class TC_LI_026_COL_submission_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Submission";

    @Test(priority = 26)
    @Description("TC-LI-026 FF-LI-MC-COL-submission-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-026_FF-LI-MC-COL-submission-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
