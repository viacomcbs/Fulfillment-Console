package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 091 | FF-MC-COL-language-SHOW: Enable "Language" (lineitem). */
public class TC_091_COL_language_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 91;
    private static final String COLUMN_ID = "language";
    private static final String COLUMN_NAME = "Language";
    private static final String SECTION = "lineitem";

    @Test(priority = 91)
    @Description("TC-091 FF-MC-COL-language-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-091_FF-MC-COL-language-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
