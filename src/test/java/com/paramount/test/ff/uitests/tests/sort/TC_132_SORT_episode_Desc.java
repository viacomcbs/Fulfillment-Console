package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 132 | FF-SORT-COL-episode-DESC: Sort "Episode" DESC (order). */
public class TC_132_SORT_episode_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 132;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 132)
    @Description("TC-132 FF-SORT-COL-episode-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-132_FF-SORT-COL-episode-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
