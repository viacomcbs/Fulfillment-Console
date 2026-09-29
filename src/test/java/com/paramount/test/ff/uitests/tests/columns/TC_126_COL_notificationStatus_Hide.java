package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 126 | FF-MC-COL-notificationStatus-HIDE: Disable "Notification Status" (lineitem). */
public class TC_126_COL_notificationStatus_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 126;
    private static final String COLUMN_ID = "notificationStatus";
    private static final String COLUMN_NAME = "Notification Status";
    private static final String SECTION = "lineitem";

    @Test(priority = 126)
    @Description("TC-126 FF-MC-COL-notificationStatus-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-126_FF-MC-COL-notificationStatus-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
