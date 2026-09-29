package com.paramount.test.ff.uitests.tests.lineitem.sort;

import com.paramount.test.ff.uitests.base.LineItemTabSortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** TC-LI-305 | FF-LI-SORT-COL-episode-ASC: Sort "Episode" ascending on Line Items tab. */
public class TC_LI_305_SORT_episode_Asc extends LineItemTabSortBaseTest {

    private static final String COLUMN_ID = "episode";
    private static final String COLUMN_NAME = "Episode";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 305)
    @Description("TC-LI-305 FF-LI-SORT-COL-episode-ASC: Verify ascending sort on Line Items tab")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-LI-305_FF-LI-SORT-COL-episode-ASC");
        runLineItemSortAscendingTest(COLUMN_NAME, COLUMN_ID, VALUE_TYPE);
        softAssert.assertAll();
    }
}
