package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 035 | FF-MC-COL-submittedBy-SHOW: Enable "Submitted by" (order). */
public class TC_035_COL_submittedBy_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 35;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";

    @Test(priority = 35)
    @Description("TC-035 FF-MC-COL-submittedBy-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-035_FF-MC-COL-submittedBy-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
