package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 099 | FF-MC-COL-errorMessage-SHOW: Enable "Error message" (lineitem). */
public class TC_099_COL_errorMessage_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 99;
    private static final String COLUMN_ID = "errorMessage";
    private static final String COLUMN_NAME = "Error message";
    private static final String SECTION = "lineitem";

    @Test(priority = 99)
    @Description("TC-099 FF-MC-COL-errorMessage-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-099_FF-MC-COL-errorMessage-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
