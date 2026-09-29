package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 020 | FF-MC-COL-xytechId-HIDE: Disable "Xytech ID" (order). */
public class TC_020_COL_xytechId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 20;
    private static final String COLUMN_ID = "xytechId";
    private static final String COLUMN_NAME = "Xytech ID";
    private static final String SECTION = "order";

    @Test(priority = 20)
    @Description("TC-020 FF-MC-COL-xytechId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-020_FF-MC-COL-xytechId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
