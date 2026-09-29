package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 166 | FF-SORT-COL-state-DESC: Sort "State" DESC (order). */
public class TC_166_SORT_state_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 166;
    private static final String COLUMN_ID = "state";
    private static final String COLUMN_NAME = "State";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 166)
    @Description("TC-166 FF-SORT-COL-state-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-166_FF-SORT-COL-state-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
