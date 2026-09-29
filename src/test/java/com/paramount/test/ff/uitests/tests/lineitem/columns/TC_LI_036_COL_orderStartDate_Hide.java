package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-036 | FF-LI-MC-COL-orderStartDate-HIDE: Disable "Order start date" on Line Items tab. */
public class TC_LI_036_COL_orderStartDate_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order start date";

    @Test(priority = 36)
    @Description("TC-LI-036 FF-LI-MC-COL-orderStartDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-036_FF-LI-MC-COL-orderStartDate-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
