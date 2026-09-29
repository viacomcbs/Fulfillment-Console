package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 012 | FF-MC-COL-titleSeasonEpisode-HIDE: Disable "Title, Season, Episode" (order). */
public class TC_012_COL_titleSeasonEpisode_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 12;
    private static final String COLUMN_ID = "titleSeasonEpisode";
    private static final String COLUMN_NAME = "Title, Season, Episode";
    private static final String SECTION = "order";

    @Test(priority = 12)
    @Description("TC-012 FF-MC-COL-titleSeasonEpisode-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-012_FF-MC-COL-titleSeasonEpisode-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
