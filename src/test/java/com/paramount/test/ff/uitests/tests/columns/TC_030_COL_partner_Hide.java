package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 030 | FF-MC-COL-partner-HIDE: Disable "Partner" (order). */
public class TC_030_COL_partner_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 30;
    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String SECTION = "order";

    @Test(priority = 30)
    @Description("TC-030 FF-MC-COL-partner-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-030_FF-MC-COL-partner-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
