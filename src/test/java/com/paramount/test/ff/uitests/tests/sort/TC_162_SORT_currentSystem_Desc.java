package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 162 | FF-SORT-COL-currentSystem-DESC: Sort "Current system" DESC (order). */
public class TC_162_SORT_currentSystem_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 162;
    private static final String COLUMN_ID = "currentSystem";
    private static final String COLUMN_NAME = "Current system";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 162)
    @Description("TC-162 FF-SORT-COL-currentSystem-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-162_FF-SORT-COL-currentSystem-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
