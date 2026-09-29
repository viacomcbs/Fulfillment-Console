package com.paramount.test.ff.uitests.tests.sort;

import com.paramount.test.ff.uitests.base.SortBaseTest;
import io.qameta.allure.Description;
import org.testng.annotations.Test;

/** Excel TC 152 | FF-SORT-COL-submittedBy-DESC: Sort "Submitted by" DESC (order). */
public class TC_152_SORT_submittedBy_Desc extends SortBaseTest {

    private static final int TC_NUMBER = 152;
    private static final String COLUMN_ID = "submittedBy";
    private static final String COLUMN_NAME = "Submitted by";
    private static final String SECTION = "order";
    private static final String VALUE_TYPE = "STRING";

    @Test(priority = 152)
    @Description("TC-152 FF-SORT-COL-submittedBy-DESC: Verify descending sort on Orders grid column")
    public void validateSortDescending() throws InterruptedException {
        initSoftAssert("TC-152_FF-SORT-COL-submittedBy-DESC");
        runSortDescendingTest(COLUMN_NAME, SECTION, VALUE_TYPE);
        softAssert.assertAll();
    }
}
