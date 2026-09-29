package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-041 | FF-LI-MC-COL-partner-SHOW: Enable "Partner" on Line Items tab. */
public class TC_LI_041_COL_partner_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Partner";

    @Test(priority = 41)
    @Description("TC-LI-041 FF-LI-MC-COL-partner-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-041_FF-LI-MC-COL-partner-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
