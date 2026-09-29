package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 046 | FF-MC-COL-currentSystem-HIDE: Disable "Current system" (order). */
public class TC_046_COL_currentSystem_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 46;
    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String SECTION = "order";

    @Test(priority = 46)
    @Description("TC-046 FF-MC-COL-currentSystem-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-046_FF-MC-COL-currentSystem-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
