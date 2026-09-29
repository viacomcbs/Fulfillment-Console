package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-306 | FF-LI-SORT-COL-episode-DESC: Sort "Episode" descending on Line Items tab. */
public class TC_LI_306_SORT_episode_Desc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 306)
    @Description("TC-LI-306 FF-LI-SORT-COL-episode-DESC: Verify descending sort on Line Items tab")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-LI-306_FF-LI-SORT-COL-episode-DESC");
        runLineItemSortDescendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
