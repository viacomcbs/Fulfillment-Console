package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 056 | FF-MC-COL-purchaseOrderId-HIDE: Disable "Purchase order ID" (order). */
public class TC_056_COL_purchaseOrderId_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 56;
    private static final String COLUMN_ID = "purchaseOrderId";
    private static final String COLUMN_NAME = "Purchase order ID";
    private static final String SECTION = "order";

    @Test(priority = 56)
    @Description("TC-056 FF-MC-COL-purchaseOrderId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-056_FF-MC-COL-purchaseOrderId-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
