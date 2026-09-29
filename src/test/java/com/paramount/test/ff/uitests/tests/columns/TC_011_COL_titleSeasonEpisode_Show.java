package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 011 | FF-MC-COL-titleSeasonEpisode-SHOW: Enable "Title, Season, Episode" (order). */
public class TC_011_COL_titleSeasonEpisode_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 11;
    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String SECTION = "order";

    @Test(priority = 11)
    @Description("TC-011 FF-MC-COL-titleSeasonEpisode-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-011_FF-MC-COL-titleSeasonEpisode-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
