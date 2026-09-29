package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 067 | FF-MC-COL-ptsPackagingId-SHOW: Enable "PTS Packaging ID" (order). */
public class TC_067_COL_ptsPackagingId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 67;
    private static final String COLUMN_ID = "ptsPackagingId";
    private static final String COLUMN_NAME = "PTS Packaging ID";
    private static final String SECTION = "order";

    @Test(priority = 67)
    @Description("TC-067 FF-MC-COL-ptsPackagingId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-067_FF-MC-COL-ptsPackagingId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
