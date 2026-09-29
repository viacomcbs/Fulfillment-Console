package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 018 | FF-MC-COL-orderId-HIDE: Disable "Order ID" (order). */
public class TC_018_COL_orderId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 18;
    private static final String COLUMN_ID = "orderId";
    private static final String COLUMN_NAME = "Order ID";
    private static final String SECTION = "order";

    @Test(priority = 18)
    @Description("TC-018 FF-MC-COL-orderId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-018_FF-MC-COL-orderId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
