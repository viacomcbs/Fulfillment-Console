package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-019 | FF-LI-MC-COL-type-SHOW: Enable "Type" on Line Items tab. */
public class TC_LI_019_COL_type_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Type";

    @Test(priority = 19)
    @Description("TC-LI-019 FF-LI-MC-COL-type-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-019_FF-LI-MC-COL-type-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
