package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 023 | FF-MC-COL-dsid-SHOW: Enable "DSID" (order). */
public class TC_023_COL_dsid_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 23;
    private static final String COLUMN_ID = "dsid";
    private static final String COLUMN_NAME = "DSID";
    private static final String SECTION = "order";

    @Test(priority = 23)
    @Description("TC-023 FF-MC-COL-dsid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-023_FF-MC-COL-dsid-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
