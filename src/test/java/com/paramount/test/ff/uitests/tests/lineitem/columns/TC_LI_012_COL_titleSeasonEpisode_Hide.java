package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-012 | FF-LI-MC-COL-titleSeasonEpisode-HIDE: Disable "Title, Season, Episode" on Line Items tab. */
public class TC_LI_012_COL_titleSeasonEpisode_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Title, Season, Episode";

    @Test(priority = 12)
    @Description("TC-LI-012 FF-LI-MC-COL-titleSeasonEpisode-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-012_FF-LI-MC-COL-titleSeasonEpisode-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
