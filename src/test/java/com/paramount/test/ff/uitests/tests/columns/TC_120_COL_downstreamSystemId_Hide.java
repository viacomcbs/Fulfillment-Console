package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 120 | FF-MC-COL-downstreamSystemId-HIDE: Disable "Downstream System ID" (lineitem). */
public class TC_120_COL_downstreamSystemId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 120;
    private static final String COLUMN_ID = "downstreamSystemId";
    private static final String COLUMN_NAME = "Downstream System ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 120)
    @Description("TC-120 FF-MC-COL-downstreamSystemId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-120_FF-MC-COL-downstreamSystemId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
