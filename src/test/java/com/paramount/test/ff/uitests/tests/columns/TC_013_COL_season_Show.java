package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 013 | FF-MC-COL-season-SHOW: Enable "Season" (order). */
public class TC_013_COL_season_Show extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 13;
    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String SECTION = "order";

    @Test(priority = 13)
    @Description("TC-013 FF-MC-COL-season-SHOW: Enable column and verify grid header visibility")
    public void validateEnableColumnShowsHeader() throws InterruptedException {
        initSoftAssert("TC-013_FF-MC-COL-season-SHOW");
        runColumnShowTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
