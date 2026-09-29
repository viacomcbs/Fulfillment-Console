package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-038 | FF-LI-MC-COL-orderEndDate-HIDE: Disable "Order end date" on Line Items tab. */
public class TC_LI_038_COL_orderEndDate_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order end date";

    @Test(priority = 38)
    @Description("TC-LI-038 FF-LI-MC-COL-orderEndDate-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-038_FF-LI-MC-COL-orderEndDate-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
