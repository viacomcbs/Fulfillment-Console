package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 042 | FF-MC-COL-orderType-HIDE: Disable "Order Type" (order). */
public class TC_042_COL_orderType_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 42;
    private static final String COLUMN_ID = "orderType";
    private static final String COLUMN_NAME = "Order Type";
    private static final String SECTION = "order";

    @Test(priority = 42)
    @Description("TC-042 FF-MC-COL-orderType-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-042_FF-MC-COL-orderType-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
