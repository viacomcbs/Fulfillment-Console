package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-037 | FF-LI-MC-COL-orderEndDate-SHOW: Enable "Order end date" on Line Items tab. */
public class TC_LI_037_COL_orderEndDate_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order end date";

    @Test(priority = 37)
    @Description("TC-LI-037 FF-LI-MC-COL-orderEndDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-037_FF-LI-MC-COL-orderEndDate-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
