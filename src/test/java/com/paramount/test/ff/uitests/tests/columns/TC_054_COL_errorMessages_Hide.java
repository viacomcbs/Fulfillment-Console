package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 054 | FF-MC-COL-errorMessages-HIDE: Disable "Error messages" (order). */
public class TC_054_COL_errorMessages_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 54;
    private static final String COLUMN_ID = "errorMessages";
    private static final String COLUMN_NAME = "Error messages";
    private static final String SECTION = "order";

    @Test(priority = 54)
    @Description("TC-054 FF-MC-COL-errorMessages-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-054_FF-MC-COL-errorMessages-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
