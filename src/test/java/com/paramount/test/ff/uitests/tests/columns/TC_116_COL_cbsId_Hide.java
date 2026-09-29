package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 116 | FF-MC-COL-cbsId-HIDE: Disable "CBS ID" (lineitem). */
public class TC_116_COL_cbsId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 116;
    private static final String COLUMN_ID = "cbsId";
    private static final String COLUMN_NAME = "CBS ID";
    private static final String SECTION = "lineitem";

    @Test(priority = 116)
    @Description("TC-116 FF-MC-COL-cbsId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-116_FF-MC-COL-cbsId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
