package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 105 | FF-MC-COL-editCrid-SHOW: Enable "Edit CRID" (lineitem). */
public class TC_105_COL_editCrid_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 105;
    private static final String COLUMN_ID = "editCrid";
    private static final String COLUMN_NAME = "Edit CRID";
    private static final String SECTION = "lineitem";

    @Test(priority = 105)
    @Description("TC-105 FF-MC-COL-editCrid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-105_FF-MC-COL-editCrid-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
