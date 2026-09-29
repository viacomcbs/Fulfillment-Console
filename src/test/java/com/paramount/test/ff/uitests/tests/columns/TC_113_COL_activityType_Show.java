package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 113 | FF-MC-COL-activityType-SHOW: Enable "Activity Type" (lineitem). */
public class TC_113_COL_activityType_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 113;
    private static final String COLUMN_ID = "activityType";
    private static final String COLUMN_NAME = "Activity Type";
    private static final String SECTION = "lineitem";

    @Test(priority = 113)
    @Description("TC-113 FF-MC-COL-activityType-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-113_FF-MC-COL-activityType-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
