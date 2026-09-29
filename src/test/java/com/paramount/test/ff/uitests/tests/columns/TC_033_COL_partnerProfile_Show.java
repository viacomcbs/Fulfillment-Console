package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 033 | FF-MC-COL-partnerProfile-SHOW: Enable "Partner Profile" (order). */
public class TC_033_COL_partnerProfile_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 33;
    private static final String COLUMN_ID = "partnerProfile";
    private static final String COLUMN_NAME = "Partner Profile";
    private static final String SECTION = "order";

    @Test(priority = 33)
    @Description("TC-033 FF-MC-COL-partnerProfile-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-033_FF-MC-COL-partnerProfile-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
