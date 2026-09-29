package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 115 | FF-MC-COL-cbsId-SHOW: Enable "CBS ID" (lineitem). */
public class TC_115_COL_cbsId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 115;
    private static final String COLUMN_ID = "cbsId";
    private static final String COLUMN_NAME = "CBS ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 115)
    @Description("TC-115 FF-MC-COL-cbsId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-115_FF-MC-COL-cbsId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
