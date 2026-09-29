package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 049 | FF-MC-COL-state-SHOW: Enable "State" (order). */
public class TC_049_COL_state_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 49;
    private static final String COLUMN_ID = "state";
    private static final String COLUMN_NAME = "State";
    private static final String SECTION = "order";

    @Test(priority = 49)
    @Description("TC-049 FF-MC-COL-state-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-049_FF-MC-COL-state-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
