package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-085 | FF-LI-MC-COL-totalSegments-SHOW: Enable "Total segments" on Line Items tab. */
public class TC_LI_085_COL_totalSegments_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Total segments";

    @Test(priority = 85)
    @Description("TC-LI-085 FF-LI-MC-COL-totalSegments-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-085_FF-LI-MC-COL-totalSegments-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
