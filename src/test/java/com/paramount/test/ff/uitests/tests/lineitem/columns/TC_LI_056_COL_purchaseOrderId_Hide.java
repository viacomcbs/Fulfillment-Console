package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-056 | FF-LI-MC-COL-purchaseOrderId-HIDE: Disable "Purchase order ID" on Line Items tab. */
public class TC_LI_056_COL_purchaseOrderId_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Purchase order ID";

    @Test(priority = 56)
    @Description("TC-LI-056 FF-LI-MC-COL-purchaseOrderId-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-056_FF-LI-MC-COL-purchaseOrderId-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
