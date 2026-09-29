package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-025 | FF-LI-MC-COL-submission-SHOW: Enable "Submission" on Line Items tab. */
public class TC_LI_025_COL_submission_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Submission";

    @Test(priority = 25)
    @Description("TC-LI-025 FF-LI-MC-COL-submission-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-025_FF-LI-MC-COL-submission-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
