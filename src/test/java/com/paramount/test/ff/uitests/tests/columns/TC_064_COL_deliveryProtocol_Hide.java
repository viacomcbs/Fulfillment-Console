package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 064 | FF-MC-COL-deliveryProtocol-HIDE: Disable "Delivery Protocol" (order). */
public class TC_064_COL_deliveryProtocol_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 64;
    private static final String COLUMN_ID = "deliveryProtocol";
    private static final String COLUMN_NAME = "Delivery Protocol";
    private static final String SECTION = "order";

    @Test(priority = 64)
    @Description("TC-064 FF-MC-COL-deliveryProtocol-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-064_FF-MC-COL-deliveryProtocol-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
