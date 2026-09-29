package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-014 | FF-LI-MC-COL-season-HIDE: Disable "Season" on Line Items tab. */
public class TC_LI_014_COL_season_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Season";

    @Test(priority = 14)
    @Description("TC-LI-014 FF-LI-MC-COL-season-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-014_FF-LI-MC-COL-season-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
