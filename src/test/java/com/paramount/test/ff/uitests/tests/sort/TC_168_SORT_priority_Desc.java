package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 168 | FF-SORT-COL-priority-DESC: Sort "Priority" DESC (order). */
public class TC_168_SORT_priority_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 168;
    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 168)
    @Description("TC-168 FF-SORT-COL-priority-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-168_FF-SORT-COL-priority-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
