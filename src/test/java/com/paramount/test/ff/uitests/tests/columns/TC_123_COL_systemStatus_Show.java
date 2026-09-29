package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 123 | FF-MC-COL-systemStatus-SHOW: Enable "System Status" (lineitem). */
public class TC_123_COL_systemStatus_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 123;
    private static final String COLUMN_ID = "systemStatus";
    private static final String COLUMN_NAME = "System Status";
    private static final String SECTION = "lineitem";

    @Test(priority = 123)
    @Description("TC-123 FF-MC-COL-systemStatus-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-123_FF-MC-COL-systemStatus-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
