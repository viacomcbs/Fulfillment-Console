package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 118 | FF-MC-COL-downstreamSystemName-HIDE: Disable "Downstream System Name" (lineitem). */
public class TC_118_COL_downstreamSystemName_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 118;
    private static final String COLUMN_ID = "downstreamSystemName";
    private static final String COLUMN_NAME = "Downstream System Name";
    private static final String SECTION = "lineitem";

    @Test(priority = 118)
    @Description("TC-118 FF-MC-COL-downstreamSystemName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-118_FF-MC-COL-downstreamSystemName-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
