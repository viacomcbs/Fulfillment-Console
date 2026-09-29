package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-093 | FF-LI-MC-COL-downstreamSystemId-SHOW: Enable "Downstream System ID" on Line Items tab. */
public class TC_LI_093_COL_downstreamSystemId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Downstream System ID";

    @Test(priority = 93)
    @Description("TC-LI-093 FF-LI-MC-COL-downstreamSystemId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-093_FF-LI-MC-COL-downstreamSystemId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
