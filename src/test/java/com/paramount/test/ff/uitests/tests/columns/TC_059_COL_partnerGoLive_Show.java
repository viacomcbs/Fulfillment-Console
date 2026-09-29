package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 059 | FF-MC-COL-partnerGoLive-SHOW: Enable "Partner go live" (order). */
public class TC_059_COL_partnerGoLive_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 59;
    private static final String COLUMN_ID = "partnerGoLive";
    private static final String COLUMN_NAME = "Partner go live";
    private static final String SECTION = "order";

    @Test(priority = 59)
    @Description("TC-059 FF-MC-COL-partnerGoLive-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-059_FF-MC-COL-partnerGoLive-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
