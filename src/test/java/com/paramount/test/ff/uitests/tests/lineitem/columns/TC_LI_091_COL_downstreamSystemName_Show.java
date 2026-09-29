package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-091 | FF-LI-MC-COL-downstreamSystemName-SHOW: Enable "Downstream System Name" on Line Items tab. */
public class TC_LI_091_COL_downstreamSystemName_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Downstream System Name";

    @Test(priority = 91)
    @Description("TC-LI-091 FF-LI-MC-COL-downstreamSystemName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-091_FF-LI-MC-COL-downstreamSystemName-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
