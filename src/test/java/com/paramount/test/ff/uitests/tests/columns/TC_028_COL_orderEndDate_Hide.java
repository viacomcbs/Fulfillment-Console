package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 028 | FF-MC-COL-orderEndDate-HIDE: Disable "Order end date" (order). */
public class TC_028_COL_orderEndDate_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 28;
    private static final String COLUMN_ID = "orderEndDate";
    private static final String COLUMN_NAME = "Order end date";
    private static final String SECTION = "order";

    @Test(priority = 28)
    @Description("TC-028 FF-MC-COL-orderEndDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-028_FF-MC-COL-orderEndDate-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
