package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 124 | FF-MC-COL-systemStatus-HIDE: Disable "System Status" (lineitem). */
public class TC_124_COL_systemStatus_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 124;
    private static final String COLUMN_ID = "systemStatus";
    private static final String COLUMN_NAME = "System Status";
    private static final String SECTION = "lineitem";

    @Test(priority = 124)
    @Description("TC-124 FF-MC-COL-systemStatus-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-124_FF-MC-COL-systemStatus-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
