package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-011 | FF-LI-MC-COL-titleSeasonEpisode-SHOW: Enable "Title, Season, Episode" on Line Items tab. */
public class TC_LI_011_COL_titleSeasonEpisode_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Title, Season, Episode";

    @Test(priority = 11)
    @Description("TC-LI-011 FF-LI-MC-COL-titleSeasonEpisode-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-011_FF-LI-MC-COL-titleSeasonEpisode-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
