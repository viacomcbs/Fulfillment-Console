package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 119 | FF-MC-COL-downstreamSystemId-SHOW: Enable "Downstream System ID" (lineitem). */
public class TC_119_COL_downstreamSystemId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 119;
    private static final String COLUMN_ID = "downstreamSystemId";
    private static final String COLUMN_NAME = "Downstream System ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 119)
    @Description("TC-119 FF-MC-COL-downstreamSystemId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-119_FF-MC-COL-downstreamSystemId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
