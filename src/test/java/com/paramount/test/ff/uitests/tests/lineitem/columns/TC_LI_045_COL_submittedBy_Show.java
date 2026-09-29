package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-045 | FF-LI-MC-COL-submittedBy-SHOW: Enable "Submitted by" on Line Items tab. */
public class TC_LI_045_COL_submittedBy_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Submitted by";

    @Test(priority = 45)
    @Description("TC-LI-045 FF-LI-MC-COL-submittedBy-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-045_FF-LI-MC-COL-submittedBy-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
