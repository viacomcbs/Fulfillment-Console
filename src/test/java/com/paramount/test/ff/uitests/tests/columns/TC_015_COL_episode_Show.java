package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 015 | FF-MC-COL-episode-SHOW: Enable "Episode" (order). */
public class TC_015_COL_episode_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 15;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";

    @Test(priority = 15)
    @Description("TC-015 FF-MC-COL-episode-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-015_FF-MC-COL-episode-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
