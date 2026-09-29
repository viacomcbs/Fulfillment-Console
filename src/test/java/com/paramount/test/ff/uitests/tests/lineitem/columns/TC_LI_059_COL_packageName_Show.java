package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-059 | FF-LI-MC-COL-packageName-SHOW: Enable "Package name" on Line Items tab. */
public class TC_LI_059_COL_packageName_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Package name";

    @Test(priority = 59)
    @Description("TC-LI-059 FF-LI-MC-COL-packageName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-059_FF-LI-MC-COL-packageName-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
