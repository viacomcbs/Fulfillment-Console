package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 174 | FF-SORT-COL-fastTrack-DESC: Sort "Fast Track" DESC (order). */
public class TC_174_SORT_fastTrack_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 174;
    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 174)
    @Description("TC-174 FF-SORT-COL-fastTrack-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-174_FF-SORT-COL-fastTrack-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
