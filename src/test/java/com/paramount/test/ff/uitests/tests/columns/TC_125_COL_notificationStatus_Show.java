package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 125 | FF-MC-COL-notificationStatus-SHOW: Enable "Notification Status" (lineitem). */
public class TC_125_COL_notificationStatus_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 125;
    private static final String COLUMN_ID = "notificationStatus";
    private static final String COLUMN_NAME = "Notification Status";
    private static final String SECTION = "lineitem";

    @Test(priority = 125)
    @Description("TC-125 FF-MC-COL-notificationStatus-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-125_FF-MC-COL-notificationStatus-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
