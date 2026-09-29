package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 066 | FF-MC-COL-deliveryOffset-HIDE: Disable "Delivery Offset" (order). */
public class TC_066_COL_deliveryOffset_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 66;
    private static final String COLUMN_ID = "deliveryOffset";
    private static final String COLUMN_NAME = "Delivery Offset";
    private static final String SECTION = "order";

    @Test(priority = 66)
    @Description("TC-066 FF-MC-COL-deliveryOffset-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-066_FF-MC-COL-deliveryOffset-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
