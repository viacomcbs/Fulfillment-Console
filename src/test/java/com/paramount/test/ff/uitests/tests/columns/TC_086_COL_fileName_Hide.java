package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 086 | FF-MC-COL-fileName-HIDE: Disable "File name" (lineitem). */
public class TC_086_COL_fileName_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 86;
    private static final String COLUMN_ID = "fileName";
    private static final String COLUMN_NAME = "File name";
    private static final String SECTION = "lineitem";

    @Test(priority = 86)
    @Description("TC-086 FF-MC-COL-fileName-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-086_FF-MC-COL-fileName-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
