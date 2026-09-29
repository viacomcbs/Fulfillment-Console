package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 173 | FF-SORT-COL-fastTrack-ASC: Sort "Fast Track" ASC (order). */
public class TC_173_SORT_fastTrack_Asc extends SortBaseTest {

    private static final int TC_NUMBER = 173;
    private static final String COLUMN_ID = "fastTrack";
    private static final String COLUMN_NAME = "Fast Track";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 173)
    @Description("TC-173 FF-SORT-COL-fastTrack-ASC: Verify ascending sort on Orders grid column")
    public void validateSortAscending() throws InterruptedException {
        initSoftAssert("TC-173_FF-SORT-COL-fastTrack-ASC");
        runSortAscendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
