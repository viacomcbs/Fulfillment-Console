package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 102 | FF-MC-COL-startTime-HIDE: Disable "Start time" (lineitem). */
public class TC_102_COL_startTime_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 102;
    private static final String COLUMN_ID = "startTime";
    private static final String COLUMN_NAME = "Start time";
    private static final String SECTION = "lineitem";

    @Test(priority = 102)
    @Description("TC-102 FF-MC-COL-startTime-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-102_FF-MC-COL-startTime-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
