package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 074 | FF-MC-COL-processingStartDate-HIDE: Disable "Processing Start Date" (package). */
public class TC_074_COL_processingStartDate_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 74;
    private static final String COLUMN_ID = "processingStartDate";
    private static final String COLUMN_NAME = "Processing Start Date";
    private static final String SECTION = "package";

    @Test(priority = 74)
    @Description("TC-074 FF-MC-COL-processingStartDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-074_FF-MC-COL-processingStartDate-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
