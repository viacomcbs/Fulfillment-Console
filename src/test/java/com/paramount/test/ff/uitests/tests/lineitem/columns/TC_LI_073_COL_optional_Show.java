package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-073 | FF-LI-MC-COL-optional-SHOW: Enable "Optional" on Line Items tab. */
public class TC_LI_073_COL_optional_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Optional";

    @Test(priority = 73)
    @Description("TC-LI-073 FF-LI-MC-COL-optional-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-073_FF-LI-MC-COL-optional-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
