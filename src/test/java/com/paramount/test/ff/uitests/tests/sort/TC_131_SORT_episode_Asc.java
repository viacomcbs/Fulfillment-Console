package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 131 | FF-SORT-COL-episode-ASC: Sort "Episode" ASC (order). */
public class TC_131_SORT_episode_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 131;
    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 131)
    @Description("TC-131 FF-SORT-COL-episode-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-131_FF-SORT-COL-episode-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
