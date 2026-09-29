package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-063 | FF-LI-MC-COL-endpoint-SHOW: Enable "Endpoint" on Line Items tab. */
public class TC_LI_063_COL_endpoint_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Endpoint";

    @Test(priority = 63)
    @Description("TC-LI-063 FF-LI-MC-COL-endpoint-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-063_FF-LI-MC-COL-endpoint-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
