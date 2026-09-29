package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 044 | FF-MC-COL-lastUpdatedOrder-HIDE: Disable "Last updated" (order). */
public class TC_044_COL_lastUpdatedOrder_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 44;
    private static final String COLUMN_ID = "lastUpdatedOrder";
    private static final String COLUMN_NAME = "Last updated";
    private static final String SECTION = "order";

    @Test(priority = 44)
    @Description("TC-044 FF-MC-COL-lastUpdatedOrder-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-044_FF-MC-COL-lastUpdatedOrder-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
