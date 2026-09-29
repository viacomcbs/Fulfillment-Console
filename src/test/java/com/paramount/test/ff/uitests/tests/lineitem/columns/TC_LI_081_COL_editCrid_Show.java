package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-081 | FF-LI-MC-COL-editCrid-SHOW: Enable "Edit CRID" on Line Items tab. */
public class TC_LI_081_COL_editCrid_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Edit CRID";

    @Test(priority = 81)
    @Description("TC-LI-081 FF-LI-MC-COL-editCrid-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-081_FF-LI-MC-COL-editCrid-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
