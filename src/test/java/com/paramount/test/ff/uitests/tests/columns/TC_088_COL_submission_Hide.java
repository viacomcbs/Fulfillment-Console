package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 088 | FF-MC-COL-submission-HIDE: Disable "Submission" (lineitem). */
public class TC_088_COL_submission_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 88;
    private static final String COLUMN_ID = "submission";
    private static final String COLUMN_NAME = "Submission";
    private static final String SECTION = "lineitem";

    @Test(priority = 88)
    @Description("TC-088 FF-MC-COL-submission-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-088_FF-MC-COL-submission-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
