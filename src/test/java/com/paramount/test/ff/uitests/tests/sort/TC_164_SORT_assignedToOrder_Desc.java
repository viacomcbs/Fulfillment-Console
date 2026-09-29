package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 164 | FF-SORT-COL-assignedToOrder-DESC: Sort "Assigned to" DESC (order). */
public class TC_164_SORT_assignedToOrder_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 164;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 164)
    @Description("TC-164 FF-SORT-COL-assignedToOrder-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-164_FF-SORT-COL-assignedToOrder-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
