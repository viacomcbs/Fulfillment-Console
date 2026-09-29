package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 045 | FF-MC-COL-currentSystem-SHOW: Enable "Current system" (order). */
public class TC_045_COL_currentSystem_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 45;
    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String SECTION = "order";

    @Test(priority = 45)
    @Description("TC-045 FF-MC-COL-currentSystem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-045_FF-MC-COL-currentSystem-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
