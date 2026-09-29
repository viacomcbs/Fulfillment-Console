package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-071 | FF-LI-MC-COL-status-SHOW: Enable "Status" on Line Items tab. */
public class TC_LI_071_COL_status_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Status";

    @Test(priority = 71)
    @Description("TC-LI-071 FF-LI-MC-COL-status-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-071_FF-LI-MC-COL-status-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
