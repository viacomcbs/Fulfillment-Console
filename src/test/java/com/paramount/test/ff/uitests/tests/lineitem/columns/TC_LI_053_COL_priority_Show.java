package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-053 | FF-LI-MC-COL-priority-SHOW: Enable "Priority" on Line Items tab. */
public class TC_LI_053_COL_priority_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Priority";

    @Test(priority = 53)
    @Description("TC-LI-053 FF-LI-MC-COL-priority-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-053_FF-LI-MC-COL-priority-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
