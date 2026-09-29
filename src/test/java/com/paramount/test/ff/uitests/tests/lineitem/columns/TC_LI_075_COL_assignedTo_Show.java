package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-075 | FF-LI-MC-COL-assignedTo-SHOW: Enable "Assigned to" on Line Items tab. */
public class TC_LI_075_COL_assignedTo_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Assigned to";

    @Test(priority = 75)
    @Description("TC-LI-075 FF-LI-MC-COL-assignedTo-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-075_FF-LI-MC-COL-assignedTo-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
