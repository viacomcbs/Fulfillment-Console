package com.paramount.test.ff.uitests.tests.columns;

import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.uitests.base.ManageColumnsBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 014 | FF-MC-COL-season-HIDE: Disable "Season" (order). */
public class TC_014_COL_season_Hide extends ManageColumnsBaseTest {

    private static final int TC_NUMBER = 14;
    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String SECTION = "order";

    @Test(priority = 14)
    @Description("TC-014 FF-MC-COL-season-HIDE: Disable column and verify grid header hidden")
    public void validateDisableColumnHidesHeader() throws InterruptedException {
        initSoftAssert("TC-014_FF-MC-COL-season-HIDE");
        runColumnHideTest(COLUMN_NAME, SECTION);
        softAssert.assertAll();
    }
}
