package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 087 | FF-MC-COL-submission-SHOW: Enable "Submission" (lineitem). */
public class TC_087_COL_submission_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 87;
    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final String SECTION = "lineitem";

    @Test(priority = 87)
    @Description("TC-087 FF-MC-COL-submission-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-087_FF-MC-COL-submission-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
