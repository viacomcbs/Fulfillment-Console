package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 111 | FF-MC-COL-totalSegments-SHOW: Enable "Total segments" (lineitem). */
public class TC_111_COL_totalSegments_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 111;
    private static final String COLUMN_ID = "totalSegments";
    private static final String COLUMN_NAME = "Total segments";
    private static final String SECTION = "lineitem";

    @Test(priority = 111)
    @Description("TC-111 FF-MC-COL-totalSegments-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-111_FF-MC-COL-totalSegments-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
