package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-013 | FF-LI-MC-COL-season-SHOW: Enable "Season" on Line Items tab. */
public class TC_LI_013_COL_season_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Season";

    @Test(priority = 13)
    @Description("TC-LI-013 FF-LI-MC-COL-season-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-013_FF-LI-MC-COL-season-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
