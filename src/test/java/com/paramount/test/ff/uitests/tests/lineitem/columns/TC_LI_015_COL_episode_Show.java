package com.paramount.test.ff.uitests.tests.lineitem.columns;

import com.paramount.test.ff.uitests.base.ManageColumnsLineItemBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-015 | FF-LI-MC-COL-episode-SHOW: Enable "Episode" on Line Items tab. */
public class TC_LI_015_COL_episode_Show extends ManageColumnsLineItemBaseTest {

    private static final String COLUMN_NAME = "Episode";

    @Test(priority = 15)
    @Description("TC-LI-015 FF-LI-MC-COL-episode-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-LI-015_FF-LI-MC-COL-episode-SHOW");
        runLineItemColumnShowTest(COLUMN_NAME);
        softAssert.assertAll();
    }
}
