package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-028 | FF-LI-MC-COL-orderIdOrder-HIDE: Disable "Order ID (Order level)" on Line Items tab. */
public class TC_LI_028_COL_orderIdOrder_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order ID (Order level)";

    @Test(priority = 28)
    @Description("TC-LI-028 FF-LI-MC-COL-orderIdOrder-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-028_FF-LI-MC-COL-orderIdOrder-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
