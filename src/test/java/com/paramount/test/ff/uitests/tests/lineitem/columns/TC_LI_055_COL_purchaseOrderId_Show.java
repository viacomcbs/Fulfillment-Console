package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-055 | FF-LI-MC-COL-purchaseOrderId-SHOW: Enable "Purchase order ID" on Line Items tab. */
public class TC_LI_055_COL_purchaseOrderId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Purchase order ID";

    @Test(priority = 55)
    @Description("TC-LI-055 FF-LI-MC-COL-purchaseOrderId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-055_FF-LI-MC-COL-purchaseOrderId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
