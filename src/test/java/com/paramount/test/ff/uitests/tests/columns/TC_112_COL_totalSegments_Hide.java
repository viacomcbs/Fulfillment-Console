package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 112 | FF-MC-COL-totalSegments-HIDE: Disable "Total segments" (lineitem). */
public class TC_112_COL_totalSegments_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 112;
    private static final String COLUMN_ID = "totalSegments";
    private static final String COLUMN_NAME = "Total segments";
    private static final String SECTION = "lineitem";

    @Test(priority = 112)
    @Description("TC-112 FF-MC-COL-totalSegments-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-112_FF-MC-COL-totalSegments-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
