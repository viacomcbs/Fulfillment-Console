package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-039 | FF-LI-MC-COL-orderLastUpdated-SHOW: Enable "Order last updated" on Line Items tab. */
public class TC_LI_039_COL_orderLastUpdated_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Order last updated";

    @Test(priority = 39)
    @Description("TC-LI-039 FF-LI-MC-COL-orderLastUpdated-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-039_FF-LI-MC-COL-orderLastUpdated-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
