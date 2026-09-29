package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 101 | FF-MC-COL-startTime-SHOW: Enable "Start time" (lineitem). */
public class TC_101_COL_startTime_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 101;
    private static final String COLUMN_ID = "startTime";
    private static final String COLUMN_NAME = "Start time";
    private static final String SECTION = "lineitem";

    @Test(priority = 101)
    @Description("TC-101 FF-MC-COL-startTime-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-101_FF-MC-COL-startTime-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
