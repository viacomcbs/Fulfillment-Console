package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 055 | FF-MC-COL-purchaseOrderId-SHOW: Enable "Purchase order ID" (order). */
public class TC_055_COL_purchaseOrderId_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 55;
    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String SECTION = "order";

    @Test(priority = 55)
    @Description("TC-055 FF-MC-COL-purchaseOrderId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-055_FF-MC-COL-purchaseOrderId-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
