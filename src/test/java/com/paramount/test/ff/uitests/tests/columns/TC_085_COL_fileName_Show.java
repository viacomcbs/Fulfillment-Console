package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 085 | FF-MC-COL-fileName-SHOW: Enable "File name" (lineitem). */
public class TC_085_COL_fileName_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 85;
    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final String SECTION = "lineitem";

    @Test(priority = 85)
    @Description("TC-085 FF-MC-COL-fileName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-085_FF-MC-COL-fileName-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
