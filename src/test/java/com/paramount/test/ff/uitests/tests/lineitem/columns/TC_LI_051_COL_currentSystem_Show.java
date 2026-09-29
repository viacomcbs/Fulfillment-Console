package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-051 | FF-LI-MC-COL-currentSystem-SHOW: Enable "Current system" on Line Items tab. */
public class TC_LI_051_COL_currentSystem_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Current system";

    @Test(priority = 51)
    @Description("TC-LI-051 FF-LI-MC-COL-currentSystem-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-051_FF-LI-MC-COL-currentSystem-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
