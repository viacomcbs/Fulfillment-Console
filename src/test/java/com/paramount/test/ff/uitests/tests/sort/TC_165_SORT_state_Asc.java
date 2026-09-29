package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 165 | FF-SORT-COL-state-ASC: Sort "State" ASC (order). */
public class TC_165_SORT_state_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 165;
    private static final String COLUMN_ID = "state";
    private static final String COLUMN_NAME = "State";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 165)
    @Description("TC-165 FF-SORT-COL-state-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-165_FF-SORT-COL-state-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
