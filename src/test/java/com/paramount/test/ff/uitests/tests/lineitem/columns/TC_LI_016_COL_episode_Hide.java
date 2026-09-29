package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-016 | FF-LI-MC-COL-episode-HIDE: Disable "Episode" on Line Items tab. */
public class TC_LI_016_COL_episode_Hide extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Episode";

    @Test(priority = 16)
    @Description("TC-LI-016 FF-LI-MC-COL-episode-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-LI-016_FF-LI-MC-COL-episode-HIDE");
        runLineItemColumnHideTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
