package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-027 | FF-LI-MC-COL-orderIdOrder-SHOW: Enable "Order ID (Order level)" on Line Items tab. */
public class TC_LI_027_COL_orderIdOrder_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order ID (Order level)";

    @Test(priority = 27)
    @Description("TC-LI-027 FF-LI-MC-COL-orderIdOrder-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-027_FF-LI-MC-COL-orderIdOrder-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
