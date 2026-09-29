package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-023 | FF-LI-MC-COL-fileName-SHOW: Enable "File name" on Line Items tab. */
public class TC_LI_023_COL_fileName_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "File name";

    @Test(priority = 23)
    @Description("TC-LI-023 FF-LI-MC-COL-fileName-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-023_FF-LI-MC-COL-fileName-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
