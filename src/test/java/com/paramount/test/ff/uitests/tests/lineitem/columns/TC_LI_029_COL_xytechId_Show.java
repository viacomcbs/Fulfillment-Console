package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-029 | FF-LI-MC-COL-xytechId-SHOW: Enable "Xytech ID" on Line Items tab. */
public class TC_LI_029_COL_xytechId_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Xytech ID";

    @Test(priority = 29)
    @Description("TC-LI-029 FF-LI-MC-COL-xytechId-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-029_FF-LI-MC-COL-xytechId-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
