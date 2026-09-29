package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-057 | FF-LI-MC-COL-fastTrack-SHOW: Enable "Fast Track" on Line Items tab. */
public class TC_LI_057_COL_fastTrack_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Fast Track";

    @Test(priority = 57)
    @Description("TC-LI-057 FF-LI-MC-COL-fastTrack-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-057_FF-LI-MC-COL-fastTrack-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
