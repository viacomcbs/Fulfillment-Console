package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-033 | FF-LI-MC-COL-dsid-SHOW: Enable "DSID" on Line Items tab. */
public class TC_LI_033_COL_dsid_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "DSID";

    @Test(priority = 33)
    @Description("TC-LI-033 FF-LI-MC-COL-dsid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-033_FF-LI-MC-COL-dsid-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
