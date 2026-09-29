package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 058 | FF-MC-COL-fastTrack-HIDE: Disable "Fast Track" (order). */
public class TC_058_COL_fastTrack_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 58;
    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String SECTION = "order";

    @Test(priority = 58)
    @Description("TC-058 FF-MC-COL-fastTrack-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-058_FF-MC-COL-fastTrack-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
