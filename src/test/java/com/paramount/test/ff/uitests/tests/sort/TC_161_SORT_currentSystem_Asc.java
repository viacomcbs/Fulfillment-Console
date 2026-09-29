package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 161 | FF-SORT-COL-currentSystem-ASC: Sort "Current system" ASC (order). */
public class TC_161_SORT_currentSystem_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 161;
    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 161)
    @Description("TC-161 FF-SORT-COL-currentSystem-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-161_FF-SORT-COL-currentSystem-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
