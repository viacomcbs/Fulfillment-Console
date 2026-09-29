package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 050 | FF-MC-COL-state-HIDE: Disable "State" (order). */
public class TC_050_COL_state_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 50;
    private static final String COLUMN_ID = "state";
    private static final String COLUMN_NAME = "State";
    private static final String SECTION = "order";

    @Test(priority = 50)
    @Description("TC-050 FF-MC-COL-state-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-050_FF-MC-COL-state-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
