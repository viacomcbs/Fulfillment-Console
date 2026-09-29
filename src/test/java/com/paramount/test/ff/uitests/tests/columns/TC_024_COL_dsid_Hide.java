package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 024 | FF-MC-COL-dsid-HIDE: Disable "DSID" (order). */
public class TC_024_COL_dsid_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 24;
    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String SECTION = "order";

    @Test(priority = 24)
    @Description("TC-024 FF-MC-COL-dsid-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-024_FF-MC-COL-dsid-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
