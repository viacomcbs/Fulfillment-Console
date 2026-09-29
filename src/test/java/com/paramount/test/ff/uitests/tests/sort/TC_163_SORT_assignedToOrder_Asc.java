package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 163 | FF-SORT-COL-assignedToOrder-ASC: Sort "Assigned to" ASC (order). */
public class TC_163_SORT_assignedToOrder_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 163;
    private static final String COLUMN_ID = "assignedToOrder";
    private static final String COLUMN_NAME = "Assigned to";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 163)
    @Description("TC-163 FF-SORT-COL-assignedToOrder-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-163_FF-SORT-COL-assignedToOrder-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
