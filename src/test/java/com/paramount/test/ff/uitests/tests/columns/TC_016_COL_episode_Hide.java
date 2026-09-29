package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 016 | FF-MC-COL-episode-HIDE: Disable "Episode" (order). */
public class TC_016_COL_episode_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 16;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";

    @Test(priority = 16)
    @Description("TC-016 FF-MC-COL-episode-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-016_FF-MC-COL-episode-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
