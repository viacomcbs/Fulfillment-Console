package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 053 | FF-MC-COL-errorMessages-SHOW: Enable "Error messages" (order). */
public class TC_053_COL_errorMessages_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 53;
    private static final String COLUMN_ID = "errorMessages";
    private static final String COLUMN_NAME = "Error messages";
    private static final String SECTION = "order";

    @Test(priority = 53)
    @Description("TC-053 FF-MC-COL-errorMessages-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-053_FF-MC-COL-errorMessages-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
