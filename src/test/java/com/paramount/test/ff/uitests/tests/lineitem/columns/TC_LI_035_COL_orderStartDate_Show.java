package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-035 | FF-LI-MC-COL-orderStartDate-SHOW: Enable "Order start date" on Line Items tab. */
public class TC_LI_035_COL_orderStartDate_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order start date";

    @Test(priority = 35)
    @Description("TC-LI-035 FF-LI-MC-COL-orderStartDate-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-035_FF-LI-MC-COL-orderStartDate-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
