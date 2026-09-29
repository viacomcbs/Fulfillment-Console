package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 073 | FF-MC-COL-processingStartDate-SHOW: Enable "Processing Start Date" (package). */
public class TC_073_COL_processingStartDate_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 73;
    private static final String COLUMN_ID = "processingStartDate";
    private static final String COLUMN_NAME = "Processing Start Date";
    private static final String SECTION = "package";

    @Test(priority = 73)
    @Description("TC-073 FF-MC-COL-processingStartDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-073_FF-MC-COL-processingStartDate-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
