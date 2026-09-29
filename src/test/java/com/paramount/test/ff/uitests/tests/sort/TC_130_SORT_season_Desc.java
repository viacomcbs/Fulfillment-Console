package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 130 | FF-SORT-COL-season-DESC: Sort "Season" DESC (order). */
public class TC_130_SORT_season_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 130;
    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 130)
    @Description("TC-130 FF-SORT-COL-season-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-130_FF-SORT-COL-season-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
