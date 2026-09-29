package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 019 | FF-MC-COL-xytechId-SHOW: Enable "Xytech ID" (order). */
public class TC_019_COL_xytechId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 19;
    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String SECTION = "order";

    @Test(priority = 19)
    @Description("TC-019 FF-MC-COL-xytechId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-019_FF-MC-COL-xytechId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
