package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 129 | FF-SORT-COL-season-ASC: Sort "Season" ASC (order). */
public class TC_129_SORT_season_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 129;
    private static final String COLUMN_ID = "season";
    private static final String COLUMN_NAME = "Season";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 129)
    @Description("TC-129 FF-SORT-COL-season-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-129_FF-SORT-COL-season-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
