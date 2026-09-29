package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 117 | FF-MC-COL-downstreamSystemName-SHOW: Enable "Downstream System Name" (lineitem). */
public class TC_117_COL_downstreamSystemName_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 117;
    private static final String COLUMN_ID = "downstreamSystemName";
    private static final String COLUMN_NAME = "Downstream System Name";
    private static final String SECTION = "lineitem";

    @Test(priority = 117)
    @Description("TC-117 FF-MC-COL-downstreamSystemName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-117_FF-MC-COL-downstreamSystemName-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
