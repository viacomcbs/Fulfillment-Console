package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 100 | FF-MC-COL-errorMessage-HIDE: Disable "Error message" (lineitem). */
public class TC_100_COL_errorMessage_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 100;
    private static final String COLUMN_ID = "errorMessage";
    private static final String COLUMN_NAME = "Error message";
    private static final String SECTION = "lineitem";

    @Test(priority = 100)
    @Description("TC-100 FF-MC-COL-errorMessage-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-100_FF-MC-COL-errorMessage-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
