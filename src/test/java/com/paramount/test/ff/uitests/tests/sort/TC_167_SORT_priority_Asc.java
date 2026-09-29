package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 167 | FF-SORT-COL-priority-ASC: Sort "Priority" ASC (order). */
public class TC_167_SORT_priority_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 167;
    private static final String COLUMN_ID = "priority";
    private static final String COLUMN_NAME = "Priority";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "NUMBER";

    @Test(priority = 167)
    @Description("TC-167 FF-SORT-COL-priority-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-167_FF-SORT-COL-priority-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
