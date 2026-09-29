package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-021 | FF-LI-MC-COL-orderType-SHOW: Enable "Order Type" on Line Items tab. */
public class TC_LI_021_COL_orderType_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order Type";

    @Test(priority = 21)
    @Description("TC-LI-021 FF-LI-MC-COL-orderType-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-021_FF-LI-MC-COL-orderType-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
