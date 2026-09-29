package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 114 | FF-MC-COL-activityType-HIDE: Disable "Activity Type" (lineitem). */
public class TC_114_COL_activityType_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 114;
    private static final String COLUMN_ID = "activityType";
    private static final String COLUMN_NAME = "Activity Type";
    private static final String SECTION = "lineitem";

    @Test(priority = 114)
    @Description("TC-114 FF-MC-COL-activityType-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-114_FF-MC-COL-activityType-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
