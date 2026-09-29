package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 057 | FF-MC-COL-fastTrack-SHOW: Enable "Fast Track" (order). */
public class TC_057_COL_fastTrack_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 57;
    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String SECTION = "order";

    @Test(priority = 57)
    @Description("TC-057 FF-MC-COL-fastTrack-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-057_FF-MC-COL-fastTrack-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
