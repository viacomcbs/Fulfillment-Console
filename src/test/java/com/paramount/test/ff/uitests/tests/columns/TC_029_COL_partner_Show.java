package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 029 | FF-MC-COL-partner-SHOW: Enable "Partner" (order). */
public class TC_029_COL_partner_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 29;
    private static final String COLUMN_ID = "partner";
    private static final String COLUMN_NAME = "Partner";
    private static final String SECTION = "order";

    @Test(priority = 29)
    @Description("TC-029 FF-MC-COL-partner-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-029_FF-MC-COL-partner-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
